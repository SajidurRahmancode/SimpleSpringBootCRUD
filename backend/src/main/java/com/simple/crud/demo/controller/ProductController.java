package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.ProductCreateDto;
import com.simple.crud.demo.model.dto.ProductResponseDto;
import com.simple.crud.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
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
    public ResponseEntity<org.springframework.data.domain.Page<ProductResponseDto>> getMyProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var products = productService.getMyProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/supplied")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<org.springframework.data.domain.Page<ProductResponseDto>> getSuppliedProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var products = productService.getSuppliedProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        Optional<ProductResponseDto> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<ProductResponseDto>> searchProducts(
            @RequestParam(name = "q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var products = productService.searchProductsByName(query, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/in-stock")
    public ResponseEntity<List<ProductResponseDto>> getProductsInStock() {
        List<ProductResponseDto> products = productService.getProductsInStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDto>> getProductsWithLowStock(@RequestParam(defaultValue = "10") Integer threshold) {
        List<ProductResponseDto> products = productService.getProductsWithLowStock(threshold);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateDto productCreateDto,
            @RequestParam(value = "sellerIdentifier", required = false) String sellerIdentifier) {
        ProductResponseDto createdProduct = productService.createProduct(productCreateDto, sellerIdentifier);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // Multipart variant for image upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductMultipart(
            @org.springframework.web.bind.annotation.ModelAttribute @Valid ProductCreateDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "sellerIdentifier", required = false) String sellerIdentifier
    ) {
        ProductResponseDto createdProduct = productService.createProduct(dto, image, sellerIdentifier);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

        @PutMapping("/{id}")
        public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateDto productCreateDto) {
        Optional<ProductResponseDto> updatedProduct = productService.updateProduct(id, productCreateDto);
        return updatedProduct.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        }

        // Multipart variant for update with image
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> updateProductMultipart(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.ModelAttribute @Valid ProductCreateDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
        ) {
        Optional<ProductResponseDto> updated = productService.updateProduct(id, dto, image);
        return updated.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> stockUpdate) {
        Integer newQuantity = stockUpdate.get("quantity");
        if (newQuantity == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Quantity is required"));
        }

        Optional<ProductResponseDto> updatedProduct = productService.updateStock(id, newQuantity);
        return updatedProduct.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
