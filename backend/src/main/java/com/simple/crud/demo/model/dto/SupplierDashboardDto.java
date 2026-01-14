package com.simple.crud.demo.model.dto;

import com.simple.crud.demo.model.entity.SupplierApplication;

import java.util.ArrayList;
import java.util.List;

public class SupplierDashboardDto {

    private boolean supplier;
    private boolean canApply;
    private SupplierApplication.Status latestStatus;
    private List<SupplierApplicationResponseDto> applications = new ArrayList<>();
    private List<SupplierAlertDto> alerts = new ArrayList<>();

    public boolean isSupplier() {
        return supplier;
    }

    public void setSupplier(boolean supplier) {
        this.supplier = supplier;
    }

    public boolean isCanApply() {
        return canApply;
    }

    public void setCanApply(boolean canApply) {
        this.canApply = canApply;
    }

    public SupplierApplication.Status getLatestStatus() {
        return latestStatus;
    }

    public void setLatestStatus(SupplierApplication.Status latestStatus) {
        this.latestStatus = latestStatus;
    }

    public List<SupplierApplicationResponseDto> getApplications() {
        return applications;
    }

    public void setApplications(List<SupplierApplicationResponseDto> applications) {
        this.applications = applications;
    }

    public List<SupplierAlertDto> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<SupplierAlertDto> alerts) {
        this.alerts = alerts;
    }
}
