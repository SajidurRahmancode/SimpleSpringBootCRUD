package com.simple.crud.demo.security;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        Optional<User> userOpt;
        try {
            Long id = Long.valueOf(usernameOrId);
            userOpt = userRepository.findById(id);
        } catch (NumberFormatException ex) {
            userOpt = userRepository.findByUsername(usernameOrId)
                    .or(() -> userRepository.findByEmail(usernameOrId));
        }

        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String role = user.getRole().name();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities);
    }
}
