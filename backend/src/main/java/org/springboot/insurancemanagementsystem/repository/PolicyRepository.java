package org.springboot.insurancemanagementsystem.repository;

import jakarta.persistence.LockModeType;
import org.springboot.insurancemanagementsystem.entitie.Customer;
import org.springboot.insurancemanagementsystem.entitie.Policy;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    boolean existsByPolicyNumber(String policyNumber);

    long countByCustomerAndPlan_Product_ProductTypeAndStatusIn(
            Customer customer,
            ProductType productType,
            List<PolicyStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Policy p WHERE p.id = :id")
    Optional<Policy> findByIdWithLock(@Param("id") Long id);

    Page<Policy> findByCustomerId(Long customerId, Pageable pageable);

    Page<Policy> findByStatus(PolicyStatus status, Pageable pageable);

    Page<Policy> findByCustomerIdAndStatus(Long customerId, PolicyStatus status, Pageable pageable);

    List<Policy> findByCustomerUserEmail(String customerEmail);
}