package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.enums.PremiumType;
import org.springframework.stereotype.Component;

/**
 * Stateless component that computes the per-period premium for a policy.
 *
 * <h3>Formula</h3>
 * <pre>
 *   annualRate      = 2 % of coverageAmount
 *   durationFactor  = sqrt(selectedDurationMonths / 12.0)
 *   annualPremium   = coverageAmount × 0.02 × durationFactor
 *   perPeriodAmount = annualPremium / periodsPerYear
 * </pre>
 *
 * The {@code durationFactor} gently rewards longer policies: a 12-month
 * policy has factor 1.0, a 24-month policy has factor ~1.41, etc.
 *
 * <h3>Periods per year by premium type</h3>
 * <ul>
 *   <li>MONTHLY     → 12</li>
 *   <li>QUARTERLY   →  4</li>
 *   <li>HALF_YEARLY →  2</li>
 *   <li>ANNUAL      →  1</li>
 * </ul>
 */
@Component
public class PremiumCalculator {

    /**
     * Calculates the per-period premium amount and rounds it to 2 decimal places.
     *
     * @param coverageAmount       the coverage amount chosen by the customer (₹)
     * @param durationMonths       the policy duration chosen by the customer (months)
     * @param premiumType          the payment cycle
     * @return per-period premium in ₹
     */
    public double calculate(double coverageAmount,
                            int durationMonths,
                            PremiumType premiumType) {

        double durationFactor  = Math.sqrt(durationMonths / 12.0);
        double annualPremium   = coverageAmount * 0.02 * durationFactor;
        int    periodsPerYear  = periodsPerYear(premiumType);
        double perPeriod       = annualPremium / periodsPerYear;

        // Round to 2 decimal places
        return Math.round(perPeriod * 100.0) / 100.0;
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private int periodsPerYear(PremiumType type) {
        return switch (type) {
            case MONTHLY     -> 12;
            case QUARTERLY   ->  4;
            case HALF_YEARLY ->  2;
            case ANNUAL      ->  1;
        };
    }
}
