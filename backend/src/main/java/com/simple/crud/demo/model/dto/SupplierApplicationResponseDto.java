package com.simple.crud.demo.model.dto;

import com.simple.crud.demo.model.entity.SupplierApplication;

import java.time.LocalDateTime;

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

    public SupplierApplicationResponseDto() {}

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SupplierApplication.Status getStatus() {
        return status;
    }

    public void setStatus(SupplierApplication.Status status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
}
