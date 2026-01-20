package com.simple.crud.demo.controller;

import com.simple.crud.demo.model.dto.SupplierApplicationDecisionDto;
import com.simple.crud.demo.model.dto.SupplierApplicationResponseDto;
import com.simple.crud.demo.model.entity.SupplierApplication;
import com.simple.crud.demo.service.SupplierApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
public class AdminSupplierController {

    private final SupplierApplicationService supplierApplicationService;

    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SupplierApplicationResponseDto>> listApplications(
            @RequestParam(value = "status", required = false) SupplierApplication.Status status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierApplicationService.getApplicationsForAdmin(status, pageable));
    }

    @PatchMapping("/applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierApplicationResponseDto> review(
            @PathVariable Long id,
            @Valid @RequestBody SupplierApplicationDecisionDto decisionDto) {
        return ResponseEntity.ok(supplierApplicationService.reviewApplication(id, decisionDto));
    }
}
