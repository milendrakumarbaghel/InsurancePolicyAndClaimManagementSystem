package org.springboot.insurancemanagementsystem.repository;

import io.micrometer.observation.ObservationFilter;
import org.springboot.insurancemanagementsystem.entitie.Claim;
import org.springboot.insurancemanagementsystem.entitie.ClaimStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimStatusHistoryRepository
        extends JpaRepository<ClaimStatusHistory, Long> {

    Page<ClaimStatusHistory> findByClaimId(
            Long claimId,
            Pageable pageable
    );

    Page<ClaimStatusHistory> findByUpdatedById(
            Long userId,
            Pageable pageable
    );

    Page<ClaimStatusHistory> findByClaim(Claim claim, Pageable pageable);
}