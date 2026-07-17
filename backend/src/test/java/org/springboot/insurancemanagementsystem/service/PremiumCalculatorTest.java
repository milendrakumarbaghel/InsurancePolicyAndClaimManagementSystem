package org.springboot.insurancemanagementsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springboot.insurancemanagementsystem.enums.PremiumType;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PremiumCalculatorTest {

    private PremiumCalculator premiumCalculator;

    @BeforeEach
    void setUp() {
        premiumCalculator = new PremiumCalculator();
    }

    @Test
    void increasingDurationDecreasesPremium() {
        double coverage = 100000.0;
        
        double premium12Months = premiumCalculator.calculate(coverage, 12, PremiumType.MONTHLY);
        double premium24Months = premiumCalculator.calculate(coverage, 24, PremiumType.MONTHLY);
        double premium36Months = premiumCalculator.calculate(coverage, 36, PremiumType.MONTHLY);

        assertTrue(premium24Months < premium12Months, "24-month premium should be lower than 12-month premium");
        assertTrue(premium36Months < premium24Months, "36-month premium should be lower than 24-month premium");
    }

    @Test
    void decreasingDurationIncreasesPremium() {
        double coverage = 100000.0;

        double premium12Months = premiumCalculator.calculate(coverage, 12, PremiumType.ANNUAL);
        double premium6Months = premiumCalculator.calculate(coverage, 6, PremiumType.ANNUAL);

        assertTrue(premium6Months > premium12Months, "6-month duration should yield a higher annual rate per period than 12-month duration");
    }
}
