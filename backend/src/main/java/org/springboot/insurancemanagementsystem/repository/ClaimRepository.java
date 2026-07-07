package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entitie.Claim;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository
        extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(
            String claimNumber
    );

    boolean existsByClaimNumber(
            String claimNumber
    );

    Page<Claim> findByPolicyId(
            Long policyId,
            Pageable pageable
    );

    Page<Claim> findByClaimStatus(
            ClaimStatus claimStatus,
            Pageable pageable
    );

    Page<Claim> findByPolicyCustomerId(
            Long customerId,
            Pageable pageable
    );

    Page<Claim> findByPolicyCustomerIdAndClaimStatus(
            Long customerId,
            ClaimStatus claimStatus,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(c.claimAmount), 0.0) FROM Claim c WHERE c.policy.id = :policyId AND c.claimStatus IN :statuses")
    Double getApprovedClaimAmountByPolicyId(@Param("policyId") Long policyId, @Param("statuses") List<ClaimStatus> statuses);

    List<Claim> findByPolicyCustomerUserEmail(String customerEmail);

    Page<Claim> findByAssignedInsuranceOperationsOfficerId(Long agentId, Pageable pageable);

    long countByAssignedInsuranceOperationsOfficerIdAndClaimStatusIn(Long agentId, Collection<ClaimStatus> statuses);

    Page<Claim> findByAssignedInsuranceOperationsOfficerEmail(String agentEmail, Pageable pageable);
}
