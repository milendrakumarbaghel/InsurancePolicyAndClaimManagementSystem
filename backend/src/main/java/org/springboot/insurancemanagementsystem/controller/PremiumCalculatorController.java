package org.springboot.insurancemanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.entitie.PolicyPlan;
import org.springboot.insurancemanagementsystem.enums.PremiumType;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.PolicyPlanRepository;
import org.springboot.insurancemanagementsystem.service.PremiumCalculator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * Exposes a lightweight endpoint for the frontend to obtain a live premium preview
 * before the customer commits to purchasing a plan.
 *
 * <pre>
 *   GET /api/plans/{planId}/calculate-premium?coverage=300000&duration=12&premiumType=MONTHLY
 *   → { "calculatedPremium": 833.33 }
 * </pre>
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class PremiumCalculatorController {

    private final PolicyPlanRepository planRepository;
    private final PremiumCalculator premiumCalculator;

    @GetMapping("/{planId}/calculate-premium")
    @PreAuthorize("hasAnyRole('ADMIN','INSURANCE_OPERATIONS_OFFICER','CUSTOMER')")
    public ResponseEntity<Map<String, Double>> calculatePremium(
            @PathVariable Long planId,
            @RequestParam Double coverage,
            @RequestParam Integer duration,
            @RequestParam String premiumType) {

        log.info("Premium preview request. planId={}, coverage={}, duration={}, premiumType={}",
                planId, coverage, duration, premiumType);

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        // Validate selections are within the plan's allowed range
        if (coverage == null || coverage <= 0) {
            throw new BusinessException("Coverage amount must be greater than zero.");
        }
        if (plan.getMinCoverageAmount() != null && coverage < plan.getMinCoverageAmount()) {
            throw new BusinessException(
                    "Coverage amount is below the plan minimum (" + plan.getMinCoverageAmount() + ").");
        }
        if (coverage > plan.getMaxCoverageAmount()) {
            throw new BusinessException(
                    "Coverage amount exceeds the plan maximum (" + plan.getMaxCoverageAmount() + ").");
        }
        if (duration == null || duration <= 0) {
            throw new BusinessException("Duration must be greater than zero.");
        }
        if (plan.getMinDuration() != null && duration < plan.getMinDuration()) {
            throw new BusinessException(
                    "Duration is below the plan minimum (" + plan.getMinDuration() + " months).");
        }
        if (duration > plan.getMaxDuration()) {
            throw new BusinessException(
                    "Duration exceeds the plan maximum (" + plan.getMaxDuration() + " months).");
        }

        PremiumType type;
        try {
            type = PremiumType.valueOf(premiumType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid premium type. Allowed: "
                    + Arrays.toString(PremiumType.values()));
        }

        double result = premiumCalculator.calculate(coverage, duration, type);

        log.info("Premium preview computed. planId={}, premium={}", planId, result);

        return ResponseEntity.ok(Map.of("calculatedPremium", result));
    }
}
