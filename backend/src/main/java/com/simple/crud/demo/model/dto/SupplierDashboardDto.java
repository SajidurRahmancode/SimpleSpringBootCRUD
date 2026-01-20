package com.simple.crud.demo.model.dto;

import com.simple.crud.demo.model.entity.SupplierApplication;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SupplierDashboardDto {

    private boolean supplier;
    private boolean canApply;
    private SupplierApplication.Status latestStatus;
    private List<SupplierApplicationResponseDto> applications = new ArrayList<>();
    private List<SupplierAlertDto> alerts = new ArrayList<>();
}
