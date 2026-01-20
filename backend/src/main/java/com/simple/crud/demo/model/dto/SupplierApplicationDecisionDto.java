package com.simple.crud.demo.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierApplicationDecisionDto {

    @NotNull
    private Decision decision;

    @Size(max = 500)
    private String adminNote;

    public enum Decision {
        APPROVE,
        REJECT
    }
}
