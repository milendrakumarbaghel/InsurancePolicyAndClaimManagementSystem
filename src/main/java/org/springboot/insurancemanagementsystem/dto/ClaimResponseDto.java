package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimResponseDto {
    private Long claimId;
    private String claimNumber;
    private Long policyId;
    private String policyNumber;
    private String customerName;
    private Double claimAmount;
    private String claimStatus;
    private String agentRemarks;
    private String adminRemarks;
    private List<ClaimDocumentResponse> documents;
}
