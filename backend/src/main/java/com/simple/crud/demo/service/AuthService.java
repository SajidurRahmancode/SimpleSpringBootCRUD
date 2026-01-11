package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.AuthResponseDto;
import com.simple.crud.demo.model.dto.LoginRequestDto;
import com.simple.crud.demo.model.dto.UserCreateDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.UserRepository;
import com.simple.crud.demo.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponseDto register(UserCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        userRepository.save(user);

        UserResponseDto userDto = new UserResponseDto(user);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user.getUsername(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        String token = jwtTokenProvider.generateToken(auth, user.getId());
        return new AuthResponseDto(token, userDto);
    }

    public AuthResponseDto login(LoginRequestDto req) {
        Optional<User> userOpt = userRepository.findByUsername(req.getIdentifier())
                .or(() -> userRepository.findByEmail(req.getIdentifier()));
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        UserResponseDto userDto = new UserResponseDto(user);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user.getUsername(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        String token = jwtTokenProvider.generateToken(auth, user.getId());
        return new AuthResponseDto(token, userDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> me(Long userId) {
        return userRepository.findById(userId).map(UserResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> meByPrincipal(String principal) {
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .map(UserResponseDto::new);
    }
}
