package com.simple.crud.demo.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SupplierApplicationDecisionDto {

    @NotNull
    private Decision decision;

    @Size(max = 500)
    private String adminNote;

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public enum Decision {
        APPROVE,
        REJECT
    }
}
