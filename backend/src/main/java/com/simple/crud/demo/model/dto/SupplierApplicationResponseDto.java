package com.simple.crud.demo.model.dto;

import com.simple.crud.demo.model.entity.SupplierApplication;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SupplierApplicationResponseDto {

    private Long id;
    private String businessName;
    private String businessEmail;
    private String businessPhone;
    private String website;
    private String message;
    private SupplierApplication.Status status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String adminNote;

    // Custom constructor for entity mapping
    public SupplierApplicationResponseDto(SupplierApplication entity) {
        this.id = entity.getId();
        this.businessName = entity.getBusinessName();
        this.businessEmail = entity.getBusinessEmail();
        this.businessPhone = entity.getBusinessPhone();
        this.website = entity.getWebsite();
        this.message = entity.getMessage();
        this.status = entity.getStatus();
        this.submittedAt = entity.getSubmittedAt();
        this.reviewedAt = entity.getReviewedAt();
        this.reviewedBy = entity.getReviewedBy();
        this.adminNote = entity.getAdminNote();
    }
}
