package com.simple.crud.demo.model.dto;

import com.simple.crud.demo.model.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime supplierSince;
    private String supplierProfile;

    // Custom constructor for entity mapping
    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.supplierSince = user.getSupplierSince();
        this.supplierProfile = user.getSupplierProfile();
    }
}
