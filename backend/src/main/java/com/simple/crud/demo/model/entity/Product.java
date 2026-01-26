package com.simple.crud.demo.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"owner"}) // Exclude lazy-loaded relationships
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "image_path")
    private String imagePath;
//FetchType.EAGER	Loads immediately
//FetchType.LAZY	Loads when the page is accessed
    @ManyToOne(fetch = FetchType.LAZY)  //many products can belong to one user
    @JoinColumn(name = "owner_id") //foreign key column
    private User owner; //owner of the product

    // Custom constructor for backward compatibility
    public Product(String name, String description, BigDecimal price, Integer stockQuantity) { // No 'id' parameter
        this();// Calls the no-args constructor to initialize default values
        this.name = name; // Sets the product name
        this.description = description; // Sets the product description
        this.price = price; // Sets the product price
        this.stockQuantity = stockQuantity; // Sets the stock quantity
        this.createdAt = LocalDateTime.now(); // Initializes createdAt timestamps
        this.updatedAt = LocalDateTime.now(); // Initializes updatedAt timestamps

    }

    @PreUpdate // JPA lifecycle callback to update timestamp
    public void preUpdate() { // Method called before entity update
        this.updatedAt = LocalDateTime.now(); // Updates the 'updatedAt' timestamp
    }
}
