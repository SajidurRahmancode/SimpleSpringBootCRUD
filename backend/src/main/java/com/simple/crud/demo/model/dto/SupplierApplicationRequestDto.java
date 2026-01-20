package com.simple.crud.demo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierApplicationRequestDto {

    @NotBlank
    @Size(max = 150)
    private String businessName;

    @NotBlank
    @Email
    private String businessEmail;

    @Size(max = 50)
    private String businessPhone;

    @Size(max = 255)
    private String website;

    @Size(max = 1000)
    private String message;
}
