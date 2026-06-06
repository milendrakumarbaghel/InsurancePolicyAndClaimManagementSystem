package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ClaimResponseDto> raiseClaim(
            @Valid @RequestBody ClaimRequestDto request,
            Authentication authentication) {

        return new ResponseEntity<>(
                claimService.raiseClaim(
                        request,
                        authentication.getName()
                ),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{claimId}/review")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ClaimResponseDto> reviewClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ClaimReviewRequestDto request,
            Authentication authentication) {

        return ResponseEntity.ok(
                claimService.reviewClaim(
                        claimId,
                        request,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{claimId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponseDto> approveClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        return ResponseEntity.ok(
                claimService.approveClaim(
                        claimId,
                        remarks,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{claimId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponseDto> rejectClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        return ResponseEntity.ok(
                claimService.rejectClaim(
                        claimId,
                        remarks,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<ClaimResponseDto> getClaimById(
            @PathVariable Long claimId) {

        return ResponseEntity.ok(
                claimService.getClaimById(claimId)
        );
    }

    @GetMapping("/number/{claimNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<ClaimResponseDto> getClaimByNumber(
            @PathVariable String claimNumber) {

        return ResponseEntity.ok(
                claimService.getClaimByNumber(claimNumber)
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ClaimResponseDto>> getMyClaims(
            Authentication authentication) {

        return ResponseEntity.ok(
                claimService.getMyClaims(
                        authentication.getName()
                )
        );
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

        return ResponseEntity.ok(
                claimService.getAllClaims(
                        page,
                        size,
                        sortBy,
                        sortDir
                )
        );
    }
}