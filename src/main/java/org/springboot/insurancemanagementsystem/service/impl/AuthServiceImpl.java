package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.LoginRequestDto;
import org.springboot.insurancemanagementsystem.dto.LoginResponseDto;
import org.springboot.insurancemanagementsystem.dto.LogoutRequestDto;
import org.springboot.insurancemanagementsystem.dto.RefreshTokenRequestDto;
import org.springboot.insurancemanagementsystem.dto.RegisterRequestDto;
import org.springboot.insurancemanagementsystem.dto.UserResponseDto;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.enums.Role;
import org.springboot.insurancemanagementsystem.exception.DuplicateResourceException;
import org.springboot.insurancemanagementsystem.exception.InvalidCredentialsException;
import org.springboot.insurancemanagementsystem.exception.UserInactiveException;
import org.springboot.insurancemanagementsystem.repository.UserRepository;
import org.springboot.insurancemanagementsystem.security.util.JwtUtil;
import org.springboot.insurancemanagementsystem.service.AuthService;
import org.springboot.insurancemanagementsystem.service.TokenBlacklistService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final UserDetailsService userDetailsService;
    private final OtpServiceImp otpServiceImp;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public UserResponseDto register(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .active(false)          // ← inactive until OTP verified
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        otpServiceImp.createAndSendOtp(savedUser);  // ← send OTP after save

        log.info("Customer registered. OTP sent. UserId={}", savedUser.getId());

        UserResponseDto map = modelMapper.map(savedUser, UserResponseDto.class);
        map.setUserId(savedUser.getId());
        return map;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {

        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {

                    log.warn("Login failed. User not found: {}", request.getEmail());

                    return new InvalidCredentialsException(
                            "Invalid email or password");
                });

        if (!user.isActive()) {

            log.warn("Inactive user attempted login: {}", request.getEmail());

            throw new UserInactiveException(
                    "User account is inactive");
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

        } catch (AuthenticationException ex) {

            log.warn("Invalid login credentials for email: {}", request.getEmail());

            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken =
                jwtUtil.generateAccessToken(userDetails);
        String refreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        log.info(
                "User logged in successfully. Email={}, Role={}",
                user.getEmail(),
                user.getRole()
        );

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getFullName())
                .expiresInMin(jwtUtil.getJwtExpiration() / 1000 / 60)
                .refreshExpiresInMin(jwtUtil.getRefreshExpirationMillis() / 1000 / 60)
                .build();
    }

    @Override
    public LoginResponseDto refreshToken(RefreshTokenRequestDto request) {

        String userEmail;

        try {
            userEmail = jwtUtil.extractUsername(request.getRefreshToken());
        } catch (Exception ex) {
            log.warn("Invalid refresh token received: {}", ex.getMessage());
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (!user.isActive()) {
            log.warn("Inactive user attempted token refresh: {}", userEmail);
            throw new UserInactiveException("User account is inactive");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        if (tokenBlacklistService.isBlacklisted(request.getRefreshToken())) {
            log.warn("Blacklisted refresh token used for email: {}", userEmail);
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        if (!jwtUtil.isRefreshTokenValid(request.getRefreshToken(), userDetails)) {
            log.warn("Refresh token validation failed for email: {}", userEmail);
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        tokenBlacklistService.blacklist(request.getRefreshToken());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getFullName())
                .expiresInMin(jwtUtil.getJwtExpiration() / 1000 / 60)
                .refreshExpiresInMin(jwtUtil.getRefreshExpirationMillis() / 1000 / 60)
                .build();
    }

    @Override
    public void logout(String authorizationHeader, LogoutRequestDto request) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authorizationHeader.substring(7));
        }

        if (request != null && request.getRefreshToken() != null) {
            tokenBlacklistService.blacklist(request.getRefreshToken());
        }
    }

    @Override
    public UserResponseDto createAgent(RegisterRequestDto request) {

        log.info("Agent creation request received for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {

            log.warn("Agent creation failed. Email already exists: {}", request.getEmail());

            throw new DuplicateResourceException(
                    "Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .role(Role.AGENT)
                .active(true)
                .build();

        user = userRepository.save(user);

        log.info(
                "Agent created successfully. UserId={}, Email={}",
                user.getId(),
                user.getEmail()
        );

        UserResponseDto map = modelMapper.map(user, UserResponseDto.class);
        map.setUserId(user.getId());
        return map;
    }
}
