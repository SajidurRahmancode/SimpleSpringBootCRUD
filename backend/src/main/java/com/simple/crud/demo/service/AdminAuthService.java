package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.AuthResponseDto;
import com.simple.crud.demo.model.dto.AdminRegisterDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import org.springframework.beans.factory.annotation.Value;
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
public class AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.admin.secret}")
    private String configuredAdminSecret;

    @Autowired
    public AdminAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponseDto registerAdmin(AdminRegisterDto userCreateDto) {
        if (!userCreateDto.getAdminSecret().equals(configuredAdminSecret)) {
            throw new RuntimeException("Invalid admin secret");
        }
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new RuntimeException("Admin username already exists: " + userCreateDto.getUsername());
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new RuntimeException("Admin email already exists: " + userCreateDto.getEmail());
        }

        User admin = new User();
        admin.setUsername(userCreateDto.getUsername());
        admin.setEmail(userCreateDto.getEmail());
        admin.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        admin.setRole(User.Role.ADMIN);

        User savedAdmin = userRepository.save(admin);
        UserResponseDto adminDto = new UserResponseDto(savedAdmin);
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
                UserResponseDto adminDto = new UserResponseDto(user);
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        adminDto.getUsername(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + adminDto.getRole().name())));
                String token = jwtTokenProvider.generateToken(auth, adminDto.getId());
                return Optional.of(new AuthResponseDto(token, adminDto));
            }
        }

        return Optional.empty();
    }

    public Optional<UserResponseDto> getAdminByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getRole() == User.Role.ADMIN)
                .map(UserResponseDto::new);
    }
}
