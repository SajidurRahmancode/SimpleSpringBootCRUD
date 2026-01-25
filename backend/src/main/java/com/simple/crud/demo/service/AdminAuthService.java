package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.AuthResponseDto;
import com.simple.crud.demo.model.dto.AdminRegisterDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import org.springframework.beans.factory.annotation.Value;
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
public class AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    @Value("${app.admin.secret}")
    private String configuredAdminSecret;

    public AuthResponseDto registerAdmin(AdminRegisterDto userCreateDto) {
        if (!userCreateDto.getAdminSecret().equals(configuredAdminSecret)) {
            log.error("SECURITY: Admin registration failed - invalid admin secret for username: {}", 
                    userCreateDto.getUsername());
            throw new RuntimeException("Invalid admin secret");
        }
        
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            log.warn("Admin registration failed - username already exists: {}", userCreateDto.getUsername());
            throw new RuntimeException("Admin username already exists: " + userCreateDto.getUsername());
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            log.warn("Admin registration failed - email already exists: {}", userCreateDto.getEmail());
            throw new RuntimeException("Admin email already exists: " + userCreateDto.getEmail());
        }

        User admin = new User();
        admin.setUsername(userCreateDto.getUsername());
        admin.setEmail(userCreateDto.getEmail());
        admin.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        admin.setRole(User.Role.ADMIN);

        User savedAdmin = userRepository.save(admin);
        log.info("AUDIT: Admin account created - adminId: {}, username: {}", 
                savedAdmin.getId(), savedAdmin.getUsername());
        
        UserResponseDto adminDto = userMapper.toDto(savedAdmin);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                adminDto.getUsername(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + adminDto.getRole().name())));
        String token = jwtTokenProvider.generateToken(auth, adminDto.getId());
        return new AuthResponseDto(token, adminDto);
    }

    public Optional<AuthResponseDto> loginAdmin(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() == User.Role.ADMIN && passwordEncoder.matches(password, user.getPassword())) {
                log.info("AUDIT: Admin logged in - adminId: {}, username: {}", 
                        user.getId(), user.getUsername());
                
                UserResponseDto adminDto = userMapper.toDto(user);
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        adminDto.getUsername(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + adminDto.getRole().name())));
                String token = jwtTokenProvider.generateToken(auth, adminDto.getId());
                return Optional.of(new AuthResponseDto(token, adminDto));
            }
        }

        log.warn("SECURITY: Admin login failed for username: {}", username);
        return Optional.empty();
    }

    public Optional<UserResponseDto> getAdminByUsername(String username) {
        Optional<UserResponseDto> result = userRepository.findByUsername(username)
                .filter(user -> user.getRole() == User.Role.ADMIN)
                .map(userMapper::toDto);
        if (result.isEmpty()) {
            log.warn("Admin profile not found for username: {}", username);
        }
        return result;
    }
}
