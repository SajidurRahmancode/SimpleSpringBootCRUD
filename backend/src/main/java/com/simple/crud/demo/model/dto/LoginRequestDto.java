package com.simple.crud.demo.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Identifier is required")
    private String identifier; // username or email
    @NotBlank(message = "Password is required")
    private String password;
}
