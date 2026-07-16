package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.PolicyRequestDto;
import org.springboot.insurancemanagementsystem.dto.PolicyResponseDto;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.security.util.SecurityUtil;
import org.springboot.insurancemanagementsystem.service.PolicyService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "http://localhost:5173")
public class PolicyController {

    private final PolicyService policyService;

    /**
     * Customer self-service: purchase a policy by choosing coverage and duration.
     *
     * POST /api/policies/purchase/{planId}?coverage=300000&duration=12
     */
    @PostMapping("/purchase/{planId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyResponseDto> purchasePolicy(
            @PathVariable Long planId,

            @RequestParam
            @NotNull(message = "Selected coverage amount is required")
            @Positive(message = "Coverage amount must be greater than zero")
            @Max(value = 999999999, message = "Coverage amount exceeds the allowable limit")
            Double coverage,

            @RequestParam
            @NotNull(message = "Selected duration is required")
            @Min(value = 1, message = "Duration must be at least 1 month")
            @Max(value = 120, message = "Duration cannot exceed 120 months")
            Integer duration,

            @RequestParam
            @jakarta.validation.constraints.NotBlank(message = "Premium cycle is required")
            String premiumType,

            Authentication authentication) {

        log.info("Policy purchase request. planId={}, coverage={}, duration={}, premiumType={}, customer={}",
                planId, coverage, duration, premiumType, authentication.getName());

        PolicyResponseDto response = policyService.purchasePolicy(
                planId, authentication.getName(), coverage, duration, premiumType);

        log.info("Policy purchased successfully. PolicyNumber={}", response.getPolicyNumber());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponseDto> issuePolicy(
            @Valid @RequestBody PolicyRequestDto request) {

        log.info("Policy issuance request received for customerId: {} and planId: {}",
                request.getCustomerId(), request.getPlanId());

        PolicyResponseDto response = policyService.issuePolicy(request);

        log.info("Policy issued successfully. Policy Number: {}", response.getPolicyNumber());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{policyId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSURANCE_OPERATIONS_OFFICER','CUSTOMER')")
    public ResponseEntity<PolicyResponseDto> getPolicyById(
            @PathVariable Long policyId, Authentication authentication) {

        log.info("Fetching policy details for policyId: {}", policyId);

        String email = authentication.getName();
        String role = SecurityUtil.extractRoleFromAuthentication(authentication);

        PolicyResponseDto response = policyService.getPolicyById(policyId, email, role);

        log.info("Policy details retrieved successfully for policyId: {}", policyId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{policyNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','INSURANCE_OPERATIONS_OFFICER','CUSTOMER')")
    public ResponseEntity<PolicyResponseDto> getPolicyByNumber(
            @PathVariable String policyNumber) {

        log.info("Fetching policy details for policyNumber: {}", policyNumber);

        PolicyResponseDto response = policyService.getPolicyByNumber(policyNumber);

        log.info("Policy retrieved successfully for policyNumber: {}", policyNumber);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PolicyResponseDto>> getMyPolicies(
            Authentication authentication) {

        log.info("Fetching policies for customer: {}", authentication.getName());

        List<PolicyResponseDto> policies = policyService.getMyPolicies(authentication.getName());

        log.info("Retrieved {} policies for customer: {}", policies.size(), authentication.getName());

        return ResponseEntity.ok(policies);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSURANCE_OPERATIONS_OFFICER')")
    public ResponseEntity<Page<PolicyResponseDto>> getAllPolicies(

            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false)     PolicyStatus status,
            @RequestParam(required = false)     Long customerId) {

        log.info("Fetching all policies | page: {}, size: {}, sortBy: {}, sortDir: {}, status: {}, customerId: {}",
                page, size, sortBy, sortDir, status, customerId);

        Page<PolicyResponseDto> policies = policyService.getAllPolicies(
                page, size, sortBy, sortDir, status, customerId);

        log.info("Retrieved {} policy records", policies.getNumberOfElements());

        return ResponseEntity.ok(policies);
    }

    @PatchMapping("/{policyId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponseDto> cancelPolicy(
            @PathVariable Long policyId) {

        log.info("Policy cancellation request received for policyId: {}", policyId);

        PolicyResponseDto response = policyService.cancelPolicy(policyId);

        log.info("Policy cancelled successfully. Policy Number: {}", response.getPolicyNumber());

        return ResponseEntity.ok(response);
    }
}