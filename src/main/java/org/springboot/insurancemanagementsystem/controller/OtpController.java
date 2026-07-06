package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.OtpResendRequestDto;
import org.springboot.insurancemanagementsystem.dto.OtpResponseDto;
import org.springboot.insurancemanagementsystem.dto.OtpVerifyRequestDto;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.UserRepository;
import org.springboot.insurancemanagementsystem.service.impl.OtpServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class OtpController {

    private final OtpServiceImpl otpServiceImpl;
    private final UserRepository userRepository;

    @PostMapping("/verify")
    public ResponseEntity<OtpResponseDto> verifyOtp(
            @Valid @RequestBody OtpVerifyRequestDto request) {

        log.info("OTP verification request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        otpServiceImpl.verifyOtp(user, request.getEmailOtp(), request.getPhoneOtp());

        // Mark user as verified
        user.setActive(true);
        user.setMobileVerified(true);
        user.setEmailVerified(true);
        userRepository.save(user);

        return ResponseEntity.ok(OtpResponseDto.builder()
                .success(true)
                .message("OTP verified successfully. Your account is now active.")
                .build());
    }

    @PostMapping("/resend")
    public ResponseEntity<OtpResponseDto> resendOtp(
            @Valid @RequestBody OtpResendRequestDto request) {

        log.info("OTP resend request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        otpServiceImpl.createAndSendOtp(user);

        return ResponseEntity.ok(OtpResponseDto.builder()
                .success(true)
                .message("OTP resent to your email and mobile number.")
                .build());
    }
}