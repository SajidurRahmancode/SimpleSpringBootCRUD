package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.SupplierAlertDto;
import com.simple.crud.demo.model.dto.SupplierApplicationDecisionDto;
import com.simple.crud.demo.model.dto.SupplierApplicationRequestDto;
import com.simple.crud.demo.model.dto.SupplierApplicationResponseDto;
import com.simple.crud.demo.model.dto.SupplierDashboardDto;
import com.simple.crud.demo.model.entity.SupplierApplication;
import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.SupplierApplicationRepository;
import com.simple.crud.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierApplicationService {

    private final SupplierApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Autowired
    public SupplierApplicationService(SupplierApplicationRepository applicationRepository,
                                      UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public SupplierApplicationResponseDto submitApplication(SupplierApplicationRequestDto dto) {
        User applicant = getCurrentUser();
        if (applicant.getRole() == User.Role.SUPPLIER || applicant.getRole() == User.Role.ADMIN) {
            throw new AccessDeniedException("You are already approved to supply products");
        }

        boolean hasPending = applicationRepository.existsByApplicantAndStatusIn(applicant,
                EnumSet.of(SupplierApplication.Status.PENDING));
        if (hasPending) {
            throw new IllegalStateException("You already have a pending application");
        }

        SupplierApplication entity = new SupplierApplication();
        entity.setApplicant(applicant);
        entity.setBusinessName(dto.getBusinessName());
        entity.setBusinessEmail(dto.getBusinessEmail());
        entity.setBusinessPhone(dto.getBusinessPhone());
        entity.setWebsite(dto.getWebsite());
        entity.setMessage(dto.getMessage());
        entity.setStatus(SupplierApplication.Status.PENDING);
        entity.setSubmittedAt(LocalDateTime.now());

        SupplierApplication saved = applicationRepository.save(entity);
        applicant.setSupplierProfile(dto.getBusinessName());
        userRepository.save(applicant);
        return new SupplierApplicationResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SupplierApplicationResponseDto> getMyApplications() {
        User me = getCurrentUser();
        return applicationRepository.findByApplicantOrderBySubmittedAtDesc(me).stream()
                .map(SupplierApplicationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDashboardDto getDashboard() {
        User me = getCurrentUser();
        List<SupplierApplication> applications = applicationRepository
                .findByApplicantOrderBySubmittedAtDesc(me);
        SupplierDashboardDto dto = new SupplierDashboardDto();
        dto.setApplications(applications.stream().map(SupplierApplicationResponseDto::new).collect(Collectors.toList()));
        dto.setSupplier(me.getRole() == User.Role.SUPPLIER);

        if (me.getRole() == User.Role.ADMIN) {
            dto.setCanApply(false);
            dto.getAlerts().add(new SupplierAlertDto("info",
                "Admins manage supplier requests from the admin dashboard.",
                LocalDateTime.now()));
            return dto;
        }

        Optional<SupplierApplication> latest = applications.stream().findFirst();
        latest.ifPresent(app -> dto.setLatestStatus(app.getStatus()));
        List<SupplierAlertDto> alerts = dto.getAlerts();

        if (dto.isSupplier()) {
            if (dto.getLatestStatus() == null) {
                dto.setLatestStatus(SupplierApplication.Status.APPROVED);
            }
            alerts.add(new SupplierAlertDto("success",
                    "Approved supplier since " + (me.getSupplierSince() != null ? me.getSupplierSince() : LocalDateTime.now()),
                    LocalDateTime.now()));
            dto.setCanApply(false);
            return dto;
        }

        boolean hasPending = applications.stream().anyMatch(a -> a.getStatus() == SupplierApplication.Status.PENDING);
        dto.setCanApply(!hasPending);
        if (hasPending) {
            alerts.add(new SupplierAlertDto("info", "Your supplier application is under review.", LocalDateTime.now()));
        } else if (latest.isPresent() && latest.get().getStatus() == SupplierApplication.Status.REJECTED) {
            alerts.add(new SupplierAlertDto("warning",
                    "Latest application rejected" + (latest.get().getAdminNote() != null ? ": " + latest.get().getAdminNote() : "."),
                    LocalDateTime.now()));
        } else {
            alerts.add(new SupplierAlertDto("info", "Apply to start supplying other sellers.", LocalDateTime.now()));
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SupplierApplicationResponseDto> getApplicationsForAdmin(
            SupplierApplication.Status status,
            org.springframework.data.domain.Pageable pageable) {
        if (status != null) {
            return applicationRepository.findByStatus(status, pageable)
                    .map(SupplierApplicationResponseDto::new);
        }
        return applicationRepository.findAll(pageable).map(SupplierApplicationResponseDto::new);
    }

    public SupplierApplicationResponseDto reviewApplication(Long id, SupplierApplicationDecisionDto decisionDto) {
        User admin = getCurrentUser();
        if (admin.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can review applications");
        }

        SupplierApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (application.getStatus() != SupplierApplication.Status.PENDING) {
            throw new IllegalStateException("Application already reviewed");
        }

        SupplierApplication.Status targetStatus = decisionDto.getDecision() == SupplierApplicationDecisionDto.Decision.APPROVE
                ? SupplierApplication.Status.APPROVED
                : SupplierApplication.Status.REJECTED;
        application.setStatus(targetStatus);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(admin.getUsername());
        application.setAdminNote(decisionDto.getAdminNote());

        if (targetStatus == SupplierApplication.Status.APPROVED) {
            User applicant = application.getApplicant();
            applicant.setRole(User.Role.SUPPLIER);
            if (applicant.getSupplierSince() == null) {
                applicant.setSupplierSince(LocalDateTime.now());
            }
            userRepository.save(applicant);
        }

        SupplierApplication saved = applicationRepository.save(application);
        return new SupplierApplicationResponseDto(saved);
    }

    private User getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        var auth = context.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Not authenticated");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("User not found"));
    }
}
