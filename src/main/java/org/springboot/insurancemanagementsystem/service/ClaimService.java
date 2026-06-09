package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.ClaimRequestDto;
import org.springboot.insurancemanagementsystem.dto.ClaimResponseDto;
import org.springboot.insurancemanagementsystem.dto.ClaimReviewRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ClaimService {

    ClaimResponseDto raiseClaim(
            ClaimRequestDto request,
            String customerEmail);

    ClaimResponseDto reviewClaim(
            Long claimId,
            ClaimReviewRequestDto request,
            String agentEmail);

    ClaimResponseDto approveClaim(
            Long claimId,
            String remarks,
            String adminEmail);

    ClaimResponseDto rejectClaim(
            Long claimId,
            String remarks,
            String adminEmail);

    ClaimResponseDto getClaimById(Long claimId);

    ClaimResponseDto getClaimByNumber(
            String claimNumber);

    List<ClaimResponseDto> getMyClaims(
            String customerEmail);

    Page<ClaimResponseDto> getAllClaims(
            int page,
            int size,
            String sortBy,
            String sortDir);
}
