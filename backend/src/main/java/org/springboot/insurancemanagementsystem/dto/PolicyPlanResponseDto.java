package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlanResponseDto {
    private Long PolicyPlanId;
    private Long productId;
    private String productName;
    private String planName;
    private Double maxCoverageAmount;
    private Double minCoverageAmount;
    private String premiumType;
    private Integer maxDuration;
    private Integer minDuration;
    private String termsAndConditions;
    private boolean active;
}

