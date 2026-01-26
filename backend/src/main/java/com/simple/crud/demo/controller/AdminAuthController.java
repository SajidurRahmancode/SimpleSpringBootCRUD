package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.AuthResponseDto;
import com.simple.crud.demo.model.dto.AdminRegisterDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import com.simple.crud.demo.service.AdminAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin authentication endpoints")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/register")
    @Operation(
            summary = "Admin registration",
            description = "Register a new admin account. Requires admin secret key configured in application.properties (app.admin.secret)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(  // Description of the request body
                    description = "Admin registration details including secret key",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AdminRegisterDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Admin account created",
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or wrong admin secret")
            }
    )
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody AdminRegisterDto userCreateDto) {
        AuthResponseDto auth = adminAuthService.registerAdmin(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(auth);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Admin login",
            description = "Authenticate admin user and receive JWT token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(  // Description of the request body
                    description = "Admin login credentials (username and password)",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    example = "{\"username\": \"admin\", \"password\": \"password123\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials or not an admin account"),
                    @ApiResponse(responseCode = "400", description = "Username and password are required")
            }
    )
    // Handles POST requests to /api/admin/auth/login for admin login
    public ResponseEntity<?> loginAdmin( 
            @RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<AuthResponseDto> auth = adminAuthService.loginAdmin(username, password);
        if (auth.isPresent()) {
            return ResponseEntity.ok(auth.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) 
            // Returns 401 Unauthorized if login fails
                    .body(Map.of("error", "Invalid admin credentials"));
        }
    }

    @GetMapping("/profile/{username}")
    @Operation(
            summary = "Get admin profile",
            description = "Retrieve admin profile information by username",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin profile found",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Admin not found")
            }
    )
    public ResponseEntity<?> getAdminProfile(
            @Parameter(
                    name = "username",
                    description = "Admin username",
                    required = true,
                    example = "admin",
                    in = ParameterIn.PATH //parameter is taken from the URL path
            )
            @PathVariable String username) {
        Optional<UserResponseDto> admin = adminAuthService.getAdminByUsername(username);

        if (admin.isPresent()) {
            return ResponseEntity.ok(admin.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
