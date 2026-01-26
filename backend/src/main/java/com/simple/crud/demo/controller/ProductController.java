package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.ProductCreateDto;
import com.simple.crud.demo.model.dto.ProductResponseDto;
import com.simple.crud.demo.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    // swagger documentation for getAllProducts endpoint
    @Operation(
            summary = "Get all products",

            // description for getAllProducts endpoint
            description = "Retrieve paginated list of all products. Supports sorting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
            } // end responses array
    )
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @Parameter(
                    name = "page",
                    description = "Page number (0-based)",
                    example = "0",
                    in = ParameterIn.QUERY // where the parameter is located
            )
            @RequestParam(value = "page", defaultValue = "0") int page,
            
            @Parameter(
                    name = "size",
                    description = "Page size",
                    example = "10",
                    in = ParameterIn.QUERY // where the parameter is located
            )
            @RequestParam(value = "size", defaultValue = "10") int size,
            
            @Parameter(
                    name = "sort",
                    description = "Sort criteria (field,direction)",
                    example = "name,asc",
                    in = ParameterIn.QUERY // where the parameter is located
            )
            @RequestParam(value = "sort", defaultValue = "id,asc") String sort) {
                // Parse sort parameter
        
        String[] sortParams = sort.split(",");
                // Determine sort direction
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                // is it descending
                ? Sort.Direction.DESC 
                // else ascending
                : Sort.Direction.ASC;
                // Create Pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
             // Fetch paginated products
        // Return response entity with products
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

        @GetMapping("/my")
        // swagger documentation for getMyProducts endpoint
    @Operation(
            summary = "Get my products",
            description = "Get all products owned by the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            }
    )
    public ResponseEntity<Page<ProductResponseDto>> getMyProducts(
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
        return ResponseEntity.ok(productService.getMyProducts(pageable));
    }

    @GetMapping("/{id}")
    // swagger documentation for getProductById endpoint
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a single product by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product found",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<ProductResponseDto> getProductById(
            @Parameter(
                    name = "id",
                    description = "Product ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id) {
        Optional<ProductResponseDto> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    // swagger documentation for searchProducts endpoint
    @Operation(
            summary = "Search products",
            description = "Search products by name (case-insensitive)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products found")
            }
    )
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @Parameter(
                    name = "q",
                    description = "Search query",
                    required = true,
                    example = "laptop",
                    in = ParameterIn.QUERY
            )
            @RequestParam(name = "q") String query,
            
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
        return ResponseEntity.ok(productService.searchProductsByName(query, pageable));
    }

    @PostMapping
    // swagger documentation for createProduct endpoint
    @Operation(
            summary = "Create product (JSON)",
            description = "Create a new product without image",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product creation details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductCreateDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            }
    )
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateDto productCreateDto) {
        try {
            ProductResponseDto created = productService.createProduct(productCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // swagger documentation for createProductMultipart endpoint
    @Operation(
            summary = "Create product with image",
            description = "Create a new product with optional image upload (max 5MB, jpg/png/gif)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created with image",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or file too large"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            }
    )
    public ResponseEntity<?> createProductMultipart(
            @ModelAttribute @Valid ProductCreateDto dto,
            @Parameter(
                    name = "image",
                    description = "Product image file (optional, max 5MB)",
                    required = false,
                    content = @Content(mediaType = "multipart/form-data")
            )
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            ProductResponseDto created = productService.createProduct(dto, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // swagger documentation for updateProduct endpoint

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product",
            description = "Update an existing product. Only owner or admin can update.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated product details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductCreateDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product updated",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<?> updateProduct(
            @Parameter(
                    name = "id",
                    description = "Product ID to update",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateDto productCreateDto) {
        try {
            Optional<ProductResponseDto> updated = productService.updateProduct(id, productCreateDto);
            if (updated.isPresent()) {
                return ResponseEntity.ok(updated.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found"));
            }
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
        }
    }

    @DeleteMapping("/{id}")
    // swagger documentation for deleteProduct endpoint
    @Operation(
            summary = "Delete product",
            description = "Delete a product. Only owner or admin can delete.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product deleted"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<?> deleteProduct(
            @Parameter(
                    name = "id",
                    description = "Product ID to delete",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found"));
            }
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
        }
    }
}
