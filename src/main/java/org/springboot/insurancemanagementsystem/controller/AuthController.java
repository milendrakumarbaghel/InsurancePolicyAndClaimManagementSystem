package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.LoginRequestDto;
import org.springboot.insurancemanagementsystem.dto.LoginResponseDto;
import org.springboot.insurancemanagementsystem.dto.RefreshTokenRequestDto;
import org.springboot.insurancemanagementsystem.dto.RegisterRequestDto;
import org.springboot.insurancemanagementsystem.dto.UserResponseDto;
import org.springboot.insurancemanagementsystem.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

    private final AuthService authService;

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

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {

        log.info("Login request received for email: {}", request.getEmail());

        LoginResponseDto response = authService.login(request);

        log.info("User logged in successfully: {}", request.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request) {

        LoginResponseDto response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createAgent(
            @Valid @RequestBody RegisterRequestDto request) {

        log.info("Agent creation request received for email: {}", request.getEmail());

        UserResponseDto response = authService.createAgent(request);

        log.info("Agent created successfully with email: {}", response.getEmail());

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }
}
