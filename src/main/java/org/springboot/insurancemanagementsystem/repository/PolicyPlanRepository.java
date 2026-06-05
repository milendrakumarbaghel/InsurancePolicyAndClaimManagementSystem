package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entitie.PolicyPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyPlanRepository
        extends JpaRepository<PolicyPlan, Long> {

    Page<PolicyPlan> findByProductId(Long productId,
                                     Pageable pageable);

    Page<PolicyPlan> findByActive(Boolean active,
                                  Pageable pageable);

    Page<PolicyPlan> findByProductIdAndActive(
            Long productId,
            Boolean active,
            Pageable pageable
    );
}