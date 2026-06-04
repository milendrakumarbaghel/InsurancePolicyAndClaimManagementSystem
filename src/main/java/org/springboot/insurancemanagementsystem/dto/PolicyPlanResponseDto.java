package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlanResponseDto {
    private Long id;
    private String productName;
    private String planName;
    private Double coverageAmount;
    private Double premiumAmount;
    private String premiumType;
    private Integer duration;
}
