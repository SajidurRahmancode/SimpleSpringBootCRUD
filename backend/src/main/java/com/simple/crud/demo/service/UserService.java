package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.UserCreateDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import com.simple.crud.demo.mapper.UserMapper;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public org.springframework.data.domain.Page<UserResponseDto> getAllUsers(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Optional<UserResponseDto> getUserById(Long id) {
        Optional<UserResponseDto> result = userRepository.findById(id)
            .map(userMapper::toDto);
        if (result.isEmpty()) {
            log.warn("User not found with id: {}", id);
        }
        return result;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Optional<UserResponseDto> getUserByUsername(String username) {
        Optional<UserResponseDto> result = userRepository.findByUsername(username)
            .map(userMapper::toDto);
        if (result.isEmpty()) {
            log.warn("User not found with username: {}", username);
        }
        return result;
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        log.info("AUDIT: Admin creating user - username: {}, email: {}", 
                userCreateDto.getUsername(), userCreateDto.getEmail());
        
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            log.warn("User creation failed - username already exists: {}", userCreateDto.getUsername());
            throw new RuntimeException("Username already exists: " + userCreateDto.getUsername());
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            log.warn("User creation failed - email already exists: {}", userCreateDto.getEmail());
            throw new RuntimeException("Email already exists: " + userCreateDto.getEmail());
        }

        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));

        User mapped = userMapper.toEntity(userCreateDto);
        mapped.setPassword(passwordEncoder.encode(mapped.getPassword()));
        mapped.setRole(User.Role.USER);
        User savedUser = userRepository.save(mapped);
        
        log.info("AUDIT: User created by admin - userId: {}, username: {}, role: {}", 
                savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
        return userMapper.toDto(savedUser);
    }

    public UserResponseDto createAdmin(UserCreateDto userCreateDto) {
        log.info("AUDIT: Creating admin user - username: {}, email: {}", 
                userCreateDto.getUsername(), userCreateDto.getEmail());
        
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            log.warn("Admin creation failed - username already exists: {}", userCreateDto.getUsername());
            throw new RuntimeException("Username already exists: " + userCreateDto.getUsername());
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            log.warn("Admin creation failed - email already exists: {}", userCreateDto.getEmail());
            throw new RuntimeException("Email already exists: " + userCreateDto.getEmail());
        }

        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        user.setRole(User.Role.ADMIN);

        User savedUser = userRepository.save(user);
        log.info("AUDIT: Admin user created - adminId: {}, username: {}", savedUser.getId(), savedUser.getUsername());
        return new UserResponseDto(savedUser);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Optional<UserResponseDto> updateUser(Long id, UserCreateDto userCreateDto) {
        log.info("AUDIT: Admin updating user - userId: {}, newUsername: {}", id, userCreateDto.getUsername());
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Check if username is being changed and if new username already exists
                    if (!existingUser.getUsername().equals(userCreateDto.getUsername()) &&
                        userRepository.existsByUsername(userCreateDto.getUsername())) {
                        log.warn("User update failed - username already exists: {}", userCreateDto.getUsername());
                        throw new RuntimeException("Username already exists: " + userCreateDto.getUsername());
                    }

                    // Check if email is being changed and if new email already exists
                    if (!existingUser.getEmail().equals(userCreateDto.getEmail()) &&
                        userRepository.existsByEmail(userCreateDto.getEmail())) {
                        log.warn("User update failed - email already exists: {}", userCreateDto.getEmail());
                        throw new RuntimeException("Email already exists: " + userCreateDto.getEmail());
                    }

                    userMapper.updateEntityFromDto(userCreateDto, existingUser);
                    if (userCreateDto.getPassword() != null && !userCreateDto.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
                    }

                    User updatedUser = userRepository.save(existingUser);
                    log.info("AUDIT: User updated - userId: {}, username: {}, email: {}", 
                            updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail());
                    return userMapper.toDto(updatedUser);
                });
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public boolean deleteUser(Long id) {
        log.info("AUDIT: Admin attempting to delete user - userId: {}", id);
        if (userRepository.existsById(id)) {
            Optional<User> userOpt = userRepository.findById(id);
            userRepository.deleteById(id);
            userOpt.ifPresent(user -> 
                log.info("AUDIT: User deleted - userId: {}, username: {}", id, user.getUsername())
            );
            return true;
        }
        log.warn("User deletion failed - user not found: {}", id);
        return false;
    }

}
