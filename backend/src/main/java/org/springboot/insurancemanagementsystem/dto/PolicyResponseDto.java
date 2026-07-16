package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponseDto {
    private Long policyId;
    private String policyNumber;
    private String customerName;
    private String planName;
    private String planPremiumType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double totalPremiumPaid;
    private String productType;

    // Plan limits – needed by the payment page to show the plan context
    private Double planMaxCoverageAmount;
    private Double planMinCoverageAmount;
    private Integer planMaxDuration;
    private Integer planMinDuration;

    // Customer's selections and the resulting per-period premium
    private Double selectedCoverageAmount;
    private Integer selectedDuration;
    private String selectedPremiumType;
    private Double calculatedPremiumAmount;
}
