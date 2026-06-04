package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimResponseDto {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private Double claimAmount;
    private String claimStatus;
    private String agentRemarks;
    private String adminRemarks;
}
