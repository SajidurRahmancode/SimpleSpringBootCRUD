package com.simple.crud.demo.model.dto;

import java.time.LocalDateTime;

public class SupplierAlertDto {

    private String severity;
    private String message;
    private LocalDateTime createdAt;

    public SupplierAlertDto() {}

    public SupplierAlertDto(String severity, String message, LocalDateTime createdAt) {
        this.severity = severity;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
