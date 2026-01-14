package com.simple.crud.demo.repository;

import com.simple.crud.demo.model.entity.SupplierApplication;
import com.simple.crud.demo.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierApplicationRepository extends JpaRepository<SupplierApplication, Long> {

    List<SupplierApplication> findByApplicantOrderBySubmittedAtDesc(User applicant);

    boolean existsByApplicantAndStatusIn(User applicant, Collection<SupplierApplication.Status> statuses);

    Page<SupplierApplication> findByStatus(SupplierApplication.Status status, Pageable pageable);

    Optional<SupplierApplication> findFirstByApplicantOrderBySubmittedAtDesc(User applicant);
}
