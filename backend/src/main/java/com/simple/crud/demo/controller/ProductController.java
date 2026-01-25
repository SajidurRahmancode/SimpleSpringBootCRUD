package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.ProductCreateDto;
import com.simple.crud.demo.model.dto.ProductResponseDto;
import com.simple.crud.demo.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")

public class ProductController {

    private final ProductService productService;

    @GetMapping
    //for swagger documentation
    @Operation(
            summary = "Get all products",
            description = "Retrieve paginated list of all products. Supports sorting.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "sort", description = "Sort criteria (field,direction)", example = "name,asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
            }
    )
    public ResponseEntity<org.springframework.data.domain.Page<ProductResponseDto>> getAllProducts(
            @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(value = "size", defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(value = "sort", defaultValue = "id,asc") String sort) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(sort.split(",")[0]).ascending());
        if (sort.endsWith(",desc")) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size,
                    org.springframework.data.domain.Sort.by(sort.split(",")[0]).descending());
        }
        var products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    // Current user's products
    @GetMapping("/my")
    //for swagger documentation
    @Operation(
            summary = "Get my products",
            description = "Get all products owned by the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            }
    )
    public ResponseEntity<org.springframework.data.domain.Page<ProductResponseDto>> getMyProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var products = productService.getMyProducts(pageable);
        return ResponseEntity.ok(products);
    }


    //get product by id
    @GetMapping("/{id}")
    //for swagger documentation
     @Operation(
            summary = "Get product by ID",
            description = "Retrieve a single product by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product found",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        Optional<ProductResponseDto> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    //for swagger documentation
    @Operation(
            summary = "Search products",
            description = "Search products by name (case-insensitive)",
            parameters = {
                    @Parameter(name = "q", description = "Search query", required = true, example = "laptop"),
                    @Parameter(name = "page", description = "Page number", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10")
            }
    )

    public ResponseEntity<org.springframework.data.domain.Page<ProductResponseDto>> searchProducts(
            @RequestParam(name = "q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var products = productService.searchProductsByName(query, pageable);
        return ResponseEntity.ok(products);
    }


    @PostMapping
    //for swagger documentation
    @Operation(
            summary = "Create product (JSON)",
            description = "Create a new product without image",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            }
    )
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateDto productCreateDto) {
        ProductResponseDto createdProduct = productService.createProduct(productCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // Multipart variant for image upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //for swagger documentation
    @Operation(
            summary = "Create product with image",
            description = "Create a new product with optional image upload (max 5MB)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created with image"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or file too large"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            }
    )
    public ResponseEntity<?> createProductMultipart(
            @org.springframework.web.bind.annotation.ModelAttribute @Valid ProductCreateDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ProductResponseDto createdProduct = productService.createProduct(dto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

        @PutMapping("/{id}")
        //for swagger documentation
        @Operation(
            summary = "Update product",
            description = "Update an existing product. Only owner or admin can update.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product updated"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
        public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateDto productCreateDto) {
        Optional<ProductResponseDto> updatedProduct = productService.updateProduct(id, productCreateDto);
        return updatedProduct.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        }


    @DeleteMapping("/{id}")
    //for swagger documentation
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

    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
