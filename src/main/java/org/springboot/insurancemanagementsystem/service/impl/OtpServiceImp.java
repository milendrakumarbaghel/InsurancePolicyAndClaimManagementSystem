package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springboot.insurancemanagementsystem.entitie.OtpVerification;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.exception.InvalidCredentialsException;
import org.springboot.insurancemanagementsystem.repository.OtpVerificationRepository;
import org.springboot.insurancemanagementsystem.service.EmailService;
import org.springboot.insurancemanagementsystem.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImp implements OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailServiceImp emailService;
    private final SmsServiceImp smsServiceImp;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes:5}")
    private long expiryMinutes;

    @Transactional
    public void createAndSendOtp(User user) {
        String emailOtp = generateOtp();
        String phoneOtp = generateOtp();

        OtpVerification otpVerification = OtpVerification.builder()
                .user(user)
                .emailOtp(emailOtp)
                .phoneOtp(phoneOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();

        otpVerificationRepository.save(otpVerification);

        emailService.sendOtp(user.getEmail(), emailOtp);
        smsServiceImp.sendOtp(user.getMobileNumber(), phoneOtp);

        log.info("OTP created and sent for userId={}", user.getId());
    }

    @Transactional
    public void verifyOtp(User user, String emailOtp, String phoneOtp) {
        OtpVerification latest = otpVerificationRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "No active OTP found. Please request a new OTP."));

        if (latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException(
                    "OTP has expired. Please request a new OTP.");
        }

        if (!latest.getEmailOtp().equals(emailOtp)) {
            throw new InvalidCredentialsException("Invalid email OTP");
        }

        if (!latest.getPhoneOtp().equals(phoneOtp)) {
            throw new InvalidCredentialsException("Invalid phone OTP");
        }

        latest.setUsed(true);
        otpVerificationRepository.save(latest);

        log.info("OTP verified successfully for userId={}", user.getId());
    }

    private String generateOtp() {
        int number = secureRandom.nextInt(900000) + 100000; // Always 6 digits
        return String.valueOf(number);
    }
}