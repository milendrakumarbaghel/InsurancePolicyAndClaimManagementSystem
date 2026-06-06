package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.insurancemanagementsystem.dto.PolicyPlanRequestDto;
import org.springboot.insurancemanagementsystem.dto.PolicyPlanResponseDto;
import org.springboot.insurancemanagementsystem.service.PolicyPlanService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PolicyPlanController {

    private final PolicyPlanService policyPlanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyPlanResponseDto> createPlan(
            @Valid @RequestBody PolicyPlanRequestDto request) {

        PolicyPlanResponseDto response =
                policyPlanService.createPlan(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyPlanResponseDto> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody PolicyPlanRequestDto request) {

        return ResponseEntity.ok(
                policyPlanService.updatePlan(
                        planId,
                        request));
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<PolicyPlanResponseDto> getPlanById(
            @PathVariable Long planId) {

        return ResponseEntity.ok(
                policyPlanService.getPlanById(planId));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<List<PolicyPlanResponseDto>> getPlansByProduct(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                policyPlanService.getPlansByProduct(productId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<Page<PolicyPlanResponseDto>> getAllPlans(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        return ResponseEntity.ok(
                policyPlanService.getAllPlans(
                        page,
                        size,
                        sortBy,
                        sortDir));
    }

    @PatchMapping("/{planId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivatePlan(
            @PathVariable Long planId) {

        policyPlanService.deactivatePlan(planId);

        return ResponseEntity.ok(
                "Policy plan deactivated successfully");
    }
}