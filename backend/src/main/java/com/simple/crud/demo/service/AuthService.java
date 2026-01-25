package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.AuthResponseDto;
import com.simple.crud.demo.model.dto.LoginRequestDto;
import com.simple.crud.demo.model.dto.UserCreateDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.UserRepository;
import com.simple.crud.demo.security.JwtTokenProvider;
import com.simple.crud.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public AuthResponseDto register(UserCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("Registration failed - username already exists: {}", dto.getUsername());
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed - email already exists: {}", dto.getEmail());
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        userRepository.save(user);
        log.info("AUDIT: User registered - userId: {}, username: {}", user.getId(), user.getUsername());

        UserResponseDto userDto = userMapper.toDto(user);
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
            log.warn("Login failed - user not found for identifier: {}", req.getIdentifier());
            throw new RuntimeException("Invalid credentials");
        }
        
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for userId: {}, username: {}", user.getId(), user.getUsername());
            throw new RuntimeException("Invalid credentials");
        }
        
        log.info("AUDIT: User logged in - userId: {}, username: {}, role: {}", 
                user.getId(), user.getUsername(), user.getRole());
        
        UserResponseDto userDto = userMapper.toDto(user);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user.getUsername(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        String token = jwtTokenProvider.generateToken(auth, user.getId());
        return new AuthResponseDto(token, userDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> me(Long userId) {
        Optional<UserResponseDto> result = userRepository.findById(userId).map(userMapper::toDto);
        if (result.isEmpty()) {
            log.warn("User profile not found for userId: {}", userId);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> meByPrincipal(String principal) {
        Optional<UserResponseDto> result = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .map(userMapper::toDto);
        if (result.isEmpty()) {
            log.warn("User profile not found for principal: {}", principal);
        }
        return result;
    }
}
