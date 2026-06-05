package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlanRequestDto {
    private Long productId;
    private String planName;
    private Double coverageAmount;
    private Double premiumAmount;
    private String premiumType;
    private Integer duration;
    private String termsAndConditions;
    private Boolean active;
}