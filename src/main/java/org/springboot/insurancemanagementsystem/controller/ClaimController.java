package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.ClaimRequestDto;
import org.springboot.insurancemanagementsystem.dto.ClaimResponseDto;
import org.springboot.insurancemanagementsystem.dto.ClaimReviewRequestDto;
import org.springboot.insurancemanagementsystem.service.ClaimService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Slf4j
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ClaimResponseDto> raiseClaim(
            @Valid @RequestBody ClaimRequestDto request,
            Authentication authentication) {

        log.info("Claim submission request received from customer: {} for policyId: {}",
                authentication.getName(),
                request.getPolicyId());

        ClaimResponseDto response =
                claimService.raiseClaim(
                        request,
                        authentication.getName()
                );

        log.info("Claim submitted successfully. Claim Number: {}",
                response.getClaimNumber());

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }


    @PutMapping("/{claimId}/review")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ClaimResponseDto> reviewClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ClaimReviewRequestDto request,
            Authentication authentication) {

        log.info("Claim review initiated by agent: {} for claimId: {}",
                authentication.getName(),
                claimId);

        ClaimResponseDto response =
                claimService.reviewClaim(
                        claimId,
                        request,
                        authentication.getName()
                );

        log.info("Claim review completed for claimId: {} with status: {}",
                claimId,
                response.getClaimStatus());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponseDto> approveClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        log.info("Claim approval initiated by admin: {} for claimId: {}",
                authentication.getName(),
                claimId);

        ClaimResponseDto response =
                claimService.approveClaim(
                        claimId,
                        remarks,
                        authentication.getName()
                );

        log.info("Claim approved successfully. ClaimId: {}", claimId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponseDto> rejectClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        log.info("Claim rejection initiated by admin: {} for claimId: {}",
                authentication.getName(),
                claimId);

        ClaimResponseDto response =
                claimService.rejectClaim(
                        claimId,
                        remarks,
                        authentication.getName()
                );

        log.info("Claim rejected successfully. ClaimId: {}", claimId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<ClaimResponseDto> getClaimById(
            @PathVariable Long claimId) {

        log.info("Fetching claim details for claimId: {}", claimId);

        ClaimResponseDto response =
                claimService.getClaimById(claimId);

        log.info("Claim details retrieved successfully for claimId: {}",
                claimId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{claimNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<ClaimResponseDto> getClaimByNumber(
            @PathVariable String claimNumber) {

        log.info("Fetching claim details using claim number: {}",
                claimNumber);

        ClaimResponseDto response =
                claimService.getClaimByNumber(claimNumber);

        log.info("Claim retrieved successfully for claim number: {}",
                claimNumber);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ClaimResponseDto>> getMyClaims(
            Authentication authentication) {

        log.info("Fetching claims for customer: {}",
                authentication.getName());

        List<ClaimResponseDto> claims =
                claimService.getMyClaims(
                        authentication.getName()
                );

        log.info("Retrieved {} claims for customer: {}",
                claims.size(),
                authentication.getName());

        return ResponseEntity.ok(claims);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Page<ClaimResponseDto>> getAllClaims(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        log.info(
                "Fetching all claims | page: {}, size: {}, sortBy: {}, sortDir: {}",
                page,
                size,
                sortBy,
                sortDir
        );

        Page<ClaimResponseDto> claims =
                claimService.getAllClaims(
                        page,
                        size,
                        sortBy,
                        sortDir
                );

        log.info("Retrieved {} claims from database",
                claims.getNumberOfElements());

        return ResponseEntity.ok(claims);
    }
}