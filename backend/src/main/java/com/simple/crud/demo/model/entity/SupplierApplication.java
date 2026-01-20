package com.simple.crud.demo.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "applicant") // Exclude lazy-loaded relationship
public class SupplierApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @NotBlank
    @Size(max = 150)
    @Column(name = "business_name")
    private String businessName;

    @NotBlank
    @Email
    @Column(name = "business_email")
    private String businessEmail;

    @Size(max = 50)
    @Column(name = "business_phone")
    private String businessPhone;

    @Size(max = 255)
    private String website;

    @Size(max = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "submitted_at")
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}


