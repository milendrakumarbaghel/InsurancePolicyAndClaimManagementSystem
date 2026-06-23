package org.springboot.insurancemanagementsystem.service.impl;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.entitie.BlacklistedToken;
import org.springboot.insurancemanagementsystem.repository.BlacklistedTokenRepository;
import org.springboot.insurancemanagementsystem.security.util.JwtUtil;
import org.springboot.insurancemanagementsystem.service.TokenBlacklistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void blacklist(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        cleanupExpiredTokens();

        String tokenHash = hashToken(token);

        if (blacklistedTokenRepository.existsByTokenHash(tokenHash)) {
            return;
        }

        try {
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .tokenHash(tokenHash)
                    .tokenType(jwtUtil.extractTokenType(token))
                    .expiresAt(jwtUtil.extractExpiration(token).toInstant())
                    .blacklistedAt(Instant.now())
                    .build();

            blacklistedTokenRepository.save(blacklistedToken);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Skipping invalid token blacklist request: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        cleanupExpiredTokens();
        return blacklistedTokenRepository.existsByTokenHash(hashToken(token));
    }

    private void cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
