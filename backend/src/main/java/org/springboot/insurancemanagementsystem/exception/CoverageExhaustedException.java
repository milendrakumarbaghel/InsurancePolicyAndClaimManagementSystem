package org.springboot.insurancemanagementsystem.exception;

import lombok.Getter;

@Getter
public class CoverageExhaustedException extends RuntimeException {
    private final Double coverageAmount;
    private final Double approvedClaimAmount;
    private final Double remainingCoverage;
    private final Double requestedAmount;

    public CoverageExhaustedException(String message, Double coverageAmount, Double approvedClaimAmount, Double remainingCoverage, Double requestedAmount) {
        super(message);
        this.coverageAmount = coverageAmount;
        this.approvedClaimAmount = approvedClaimAmount;
        this.remainingCoverage = remainingCoverage;
        this.requestedAmount = requestedAmount;
    }

}