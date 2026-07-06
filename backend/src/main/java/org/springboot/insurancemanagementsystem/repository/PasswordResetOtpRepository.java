package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entitie.PasswordResetOtp;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("UPDATE PasswordResetOtp otp SET otp.used = true WHERE otp.user = :user AND otp.used = false")
    void markUnusedOtpsAsUsed(@Param("user") User user);
}
