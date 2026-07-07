package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.ForgotPasswordRequestDto;
import org.springboot.insurancemanagementsystem.dto.LoginRequestDto;
import org.springboot.insurancemanagementsystem.dto.LoginResponseDto;
import org.springboot.insurancemanagementsystem.dto.LogoutRequestDto;
import org.springboot.insurancemanagementsystem.dto.OtpResponseDto;
import org.springboot.insurancemanagementsystem.dto.RefreshTokenRequestDto;
import org.springboot.insurancemanagementsystem.dto.RegisterRequestDto;
import org.springboot.insurancemanagementsystem.dto.ResetPasswordRequestDto;
import org.springboot.insurancemanagementsystem.dto.UserResponseDto;
import org.springboot.insurancemanagementsystem.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @RateLimiter(name = "authRateLimiter")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {

        log.info("Registration request received for email: {}", request.getEmail());

        UserResponseDto response = authService.register(request);

        log.info("User registered successfully with email: {}", response.getEmail());

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    @RateLimiter(name = "authRateLimiter")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {

        log.info("Login request received for email: {}", request.getEmail());

        LoginResponseDto response = authService.login(request);

        log.info("User logged in successfully: {}", request.getEmail());

        return ResponseEntity.ok(response);
    }

    @RateLimiter(name = "authRateLimiter")
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request) {

        LoginResponseDto response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @RateLimiter(name = "authRateLimiter")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) LogoutRequestDto request) {

        authService.logout(authorizationHeader, request);

        return ResponseEntity.noContent().build();
    }

    @RateLimiter(name = "authRateLimiter")
    @PostMapping("/forgot-password")
    public ResponseEntity<OtpResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {

        log.info("Forgot password request received for email: {}", request.getEmail());

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                OtpResponseDto.builder()
                        .success(true)
                        .message("If the email exists, a password reset OTP has been sent.")
                        .build());
    }

    @RateLimiter(name = "authRateLimiter")
    @PostMapping("/reset-password")
    public ResponseEntity<OtpResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {

        log.info("Password reset confirmation received for email: {}", request.getEmail());

        authService.resetPassword(request);

        return ResponseEntity.ok(
                OtpResponseDto.builder()
                        .success(true)
                        .message("Password reset successfully.")
                        .build());
    }

    @PostMapping("/insurance-operations-officers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createAgent(
            @Valid @RequestBody RegisterRequestDto request) {

        log.info("Insurance Operations Officer creation request received for email: {}", request.getEmail());

        UserResponseDto response = authService.createInsuranceOperationsOfficer(request);

        log.info("Insurance Operations Officer created successfully with email: {}", response.getEmail());

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }
}
