package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class PolicyPlanController {

    private final PolicyPlanService policyPlanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyPlanResponseDto> createPlan(
            @Valid @RequestBody PolicyPlanRequestDto request) {

        log.info(
                "Policy plan creation request received for plan: {} and productId: {}",
                request.getPlanName(),
                request.getProductId()
        );

        PolicyPlanResponseDto response =
                policyPlanService.createPlan(request);

        log.info(
                "Policy plan created successfully. Plan ID: {}, Plan Name: {}",
                response.getPolicyPlanId(),
                response.getPlanName()
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyPlanResponseDto> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody PolicyPlanRequestDto request) {

        log.info(
                "Policy plan update request received for planId: {}",
                planId
        );

        PolicyPlanResponseDto response =
                policyPlanService.updatePlan(
                        planId,
                        request);

        log.info(
                "Policy plan updated successfully. Plan ID: {}",
                planId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<PolicyPlanResponseDto> getPlanById(
            @PathVariable Long planId) {

        log.info(
                "Fetching policy plan details for planId: {}",
                planId
        );

        PolicyPlanResponseDto response =
                policyPlanService.getPlanById(planId);

        log.info(
                "Policy plan details retrieved successfully for planId: {}",
                planId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<List<PolicyPlanResponseDto>> getPlansByProduct(
            @PathVariable Long productId) {

        log.info(
                "Fetching policy plans for productId: {}",
                productId
        );

        List<PolicyPlanResponseDto> plans =
                policyPlanService.getPlansByProduct(productId);

        log.info(
                "Retrieved {} policy plans for productId: {}",
                plans.size(),
                productId
        );

        return ResponseEntity.ok(plans);
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

        log.info(
                "Fetching all policy plans | page: {}, size: {}, sortBy: {}, sortDir: {}",
                page,
                size,
                sortBy,
                sortDir
        );

        Page<PolicyPlanResponseDto> plans =
                policyPlanService.getAllPlans(
                        page,
                        size,
                        sortBy,
                        sortDir);

        log.info(
                "Retrieved {} policy plan records",
                plans.getNumberOfElements()
        );

        return ResponseEntity.ok(plans);
    }

    @PatchMapping("/{planId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivatePlan(
            @PathVariable Long planId) {

        log.info(
                "Policy plan deactivation request received for planId: {}",
                planId
        );

        policyPlanService.deactivatePlan(planId);

        log.info(
                "Policy plan deactivated successfully. Plan ID: {}",
                planId
        );

        return ResponseEntity.ok(
                "Policy plan deactivated successfully");
    }

    @PatchMapping("/{planId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activatePlan(
            @PathVariable Long planId) {

        log.info(
                "Policy plan activation request received for planId: {}",
                planId
        );

        policyPlanService.deactivatePlan(planId);

        log.info(
                "Policy plan activated successfully. Plan ID: {}",
                planId
        );

        return ResponseEntity.ok(
                "Policy plan deactivated successfully");
    }
}