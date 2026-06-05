package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entitie.Policy;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    boolean existsByPolicyNumber(String policyNumber);

    Page<Policy> findByCustomerId(Long customerId, Pageable pageable);

    Page<Policy> findByStatus(PolicyStatus status, Pageable pageable);

    Page<Policy> findByCustomerIdAndStatus(Long customerId, PolicyStatus status, Pageable pageable);

    List<Policy> findByCustomerUserEmail(String customerEmail);
}