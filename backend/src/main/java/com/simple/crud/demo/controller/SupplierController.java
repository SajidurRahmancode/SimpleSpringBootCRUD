package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.SupplierApplicationRequestDto;
import com.simple.crud.demo.model.dto.SupplierApplicationResponseDto;
import com.simple.crud.demo.model.dto.SupplierDashboardDto;
import com.simple.crud.demo.service.SupplierApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierApplicationService supplierApplicationService;

    @Autowired
    public SupplierController(SupplierApplicationService supplierApplicationService) {
        this.supplierApplicationService = supplierApplicationService;
    }

    @PostMapping("/applications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupplierApplicationResponseDto> apply(@Valid @RequestBody SupplierApplicationRequestDto dto) {
        return ResponseEntity.ok(supplierApplicationService.submitApplication(dto));
    }

    @GetMapping("/applications/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SupplierApplicationResponseDto>> myApplications() {
        return ResponseEntity.ok(supplierApplicationService.getMyApplications());
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupplierDashboardDto> dashboard() {
        return ResponseEntity.ok(supplierApplicationService.getDashboard());
    }
}
