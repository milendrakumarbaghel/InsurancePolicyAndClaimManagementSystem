package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.insurancemanagementsystem.dto.PolicyRequestDto;
import org.springboot.insurancemanagementsystem.dto.PolicyResponseDto;
import org.springboot.insurancemanagementsystem.service.PolicyService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/purchase/{planId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyResponseDto> purchasePolicy(
            @PathVariable Long planId,
            Authentication authentication) {

        PolicyResponseDto response =
                policyService.purchasePolicy(
                        planId,
                        authentication.getName());

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @PostMapping("/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponseDto> issuePolicy(
            @Valid @RequestBody PolicyRequestDto request) {

        PolicyResponseDto response =
                policyService.issuePolicy(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @GetMapping("/{policyId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<PolicyResponseDto> getPolicyById(
            @PathVariable Long policyId) {

        return ResponseEntity.ok(
                policyService.getPolicyById(policyId));
    }

    @GetMapping("/number/{policyNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<PolicyResponseDto> getPolicyByNumber(
            @PathVariable String policyNumber) {

        return ResponseEntity.ok(
                policyService.getPolicyByNumber(policyNumber));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PolicyResponseDto>> getMyPolicies(
            Authentication authentication) {

        return ResponseEntity.ok(
                policyService.getMyPolicies(
                        authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Page<PolicyResponseDto>> getAllPolicies(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        return ResponseEntity.ok(
                policyService.getAllPolicies(
                        page,
                        size,
                        sortBy,
                        sortDir));
    }

    @PatchMapping("/{policyId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<PolicyResponseDto> cancelPolicy(
            @PathVariable Long policyId) {

        return ResponseEntity.ok(
                policyService.cancelPolicy(policyId));
    }
}