package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entitie.PremiumPayment;
import org.springboot.insurancemanagementsystem.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumPaymentRepository
        extends JpaRepository<PremiumPayment, Long> {

    boolean existsByTransactionReference(
            String transactionReference
    );

    Page<PremiumPayment> findByPolicyId(
            Long policyId,
            Pageable pageable
    );

    Page<PremiumPayment> findByStatus(
            PaymentStatus status,
            Pageable pageable
    );

    Page<PremiumPayment> findByPolicyIdAndStatus(
            Long policyId,
            PaymentStatus status,
            Pageable pageable
    );
}