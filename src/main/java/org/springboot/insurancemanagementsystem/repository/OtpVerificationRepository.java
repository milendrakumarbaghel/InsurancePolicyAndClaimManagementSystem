package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entity.OtpVerification;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}