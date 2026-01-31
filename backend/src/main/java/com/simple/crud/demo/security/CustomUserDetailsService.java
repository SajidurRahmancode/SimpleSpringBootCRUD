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

// @Service marks this as a Spring service component (business logic layer)
@Service
// This class implements UserDetailsService, which Spring Security uses to load user data
public class CustomUserDetailsService implements UserDetailsService {

    // Final field to hold the UserRepository (final means it can't be changed after initialization)
    private final UserRepository userRepository;

    // @Autowired tells Spring to inject the UserRepository dependency
    // Constructor-based injection (recommended best practice)
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        // Assign the injected repository to our field
        this.userRepository = userRepository;
    }

    // @Override indicates we're implementing the method from UserDetailsService interface
    // This method is called by Spring Security when a user tries to authenticate
    @Override
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        // Declare an Optional to hold the User (might be empty if not found)
        Optional<User> userOpt;
        // Try to parse the input as a numeric ID first
        try {
            // Attempt to convert the string to a Long (user ID)
            Long id = Long.valueOf(usernameOrId);
            // If successful, search for user by ID in the database
            userOpt = userRepository.findById(id);
        } catch (NumberFormatException ex) {
            // If parsing fails (input is not a number), treat it as username or email
            // First try to find by username, if not found, then try by email
            userOpt = userRepository.findByUsername(usernameOrId)
                    // .or() is called only if findByUsername returns empty Optional
                    .or(() -> userRepository.findByEmail(usernameOrId));
        }

        // Extract the User from Optional, or throw exception if not found
        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Get the role name from the user's role enum (e.g., "ADMIN", "USER")
        String role = user.getRole().name();
        // Create a list with one authority, prefixing with "ROLE_" (Spring Security convention)
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        // Create and return Spring Security's UserDetails object with username, password, and authorities
        // This is what Spring Security uses to perform authentication and authorization
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities);
    }
}
