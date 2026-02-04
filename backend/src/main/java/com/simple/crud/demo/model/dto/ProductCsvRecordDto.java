package com.simple.crud.demo.model.dto; // DTO package

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // Lombok: generates boilerplate (getters/setters/etc.)
@Builder // Lombok: builder for constructing instances
@NoArgsConstructor // Lombok: no-args constructor
@AllArgsConstructor // Lombok: all-args constructor
public class ProductCsvRecordDto { // Represents one row from the uploaded CSV
    @NotBlank(message = "Product name is required") // Must not be blank
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters") // Enforce length range
    private String name; // Product name column
    @Size(max = 500, message = "Description cannot exceed 500 characters") // Optional description length cap
    private String description; // Product description column
    @NotNull(message = "Price is required") // Must be present
    @Positive(message = "Price must be positive") // Must be > 0
    private BigDecimal price; // Product price column
    @NotNull(message = "Stock quantity is required") // Must be present
    private Integer stockQuantity; // Product stock quantity column
}
 
