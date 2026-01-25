package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.UserResponseDto;
import com.simple.crud.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Retrieve paginated list of all users. Admin only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
            }
    )
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @Parameter(
                    name = "page",
                    description = "Page number (0-based)",
                    example = "0",
                    in = ParameterIn.QUERY
            )
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(
                    name = "size",
                    description = "Page size",
                    example = "10",
                    in = ParameterIn.QUERY
            )
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by ID. Admin only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(
                    name = "id",
                    description = "User ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id) {
        Optional<UserResponseDto> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user",
            description = "Delete a user account. Admin only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User deleted"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<?> deleteUser(
            @Parameter(
                    name = "id",
                    description = "User ID to delete",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}