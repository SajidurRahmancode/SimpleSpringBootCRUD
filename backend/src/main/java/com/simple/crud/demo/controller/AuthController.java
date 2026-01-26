package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.AuthResponseDto;
import com.simple.crud.demo.model.dto.LoginRequestDto;
import com.simple.crud.demo.model.dto.UserCreateDto;
import com.simple.crud.demo.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with USER role",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(  // Description of the request body
                    description = "User registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserCreateDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
            }
    )
    public ResponseEntity<?> register(
            @Valid @RequestBody UserCreateDto dto) {
        try {
            AuthResponseDto auth = authService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(auth);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user and receive JWT token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials (username and password)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto req) {
        try {
            AuthResponseDto auth = authService.login(req);
            return ResponseEntity.ok(auth);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Get authenticated user's profile information",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            }
    )
    public ResponseEntity<?> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        String principal = authentication.getName();
        return authService.meByPrincipal(principal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found")));
    }
}