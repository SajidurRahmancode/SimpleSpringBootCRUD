package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.UserCreateDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import com.simple.crud.demo.mapper.UserMapper;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<UserResponseDto> getAllUsers(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(userMapper::toDto);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new RuntimeException("Username already exists: " + userCreateDto.getUsername());
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
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
        return userMapper.toDto(savedUser);
    }

    public UserResponseDto createAdmin(UserCreateDto userCreateDto) {
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new RuntimeException("Username already exists: " + userCreateDto.getUsername());
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + userCreateDto.getEmail());
        }

        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        user.setRole(User.Role.ADMIN);

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Optional<UserResponseDto> updateUser(Long id, UserCreateDto userCreateDto) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Check if username is being changed and if new username already exists
                    if (!existingUser.getUsername().equals(userCreateDto.getUsername()) &&
                        userRepository.existsByUsername(userCreateDto.getUsername())) {
                        throw new RuntimeException("Username already exists: " + userCreateDto.getUsername());
                    }

                    // Check if email is being changed and if new email already exists
                    if (!existingUser.getEmail().equals(userCreateDto.getEmail()) &&
                        userRepository.existsByEmail(userCreateDto.getEmail())) {
                        throw new RuntimeException("Email already exists: " + userCreateDto.getEmail());
                    }

                    userMapper.updateEntityFromDto(userCreateDto, existingUser);
                    if (userCreateDto.getPassword() != null && !userCreateDto.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
                    }

                    User updatedUser = userRepository.save(existingUser);
                    return userMapper.toDto(updatedUser);
                });
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Validation moved to AuthController
}
