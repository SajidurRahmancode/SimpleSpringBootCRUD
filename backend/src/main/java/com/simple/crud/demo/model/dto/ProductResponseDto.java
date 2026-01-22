package com.simple.crud.demo.model.dto;

import com.simple.crud.demo.model.entity.Product;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long ownerId;
    private String ownerUsername;

    // Custom constructor for entity mapping
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.imagePath = product.getImagePath();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        if (product.getOwner() != null) {
            this.ownerId = product.getOwner().getId();
            this.ownerUsername = product.getOwner().getUsername();
        }
    }
}
