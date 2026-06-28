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
    private String incidentDate;
    private String customerName;
    private Double claimAmount;
    private String claimReason;
    private String claimStatus;
    private String agentRemarks;
    private String adminRemarks;
    private List<ClaimDocumentResponse> documents;

    // Enriched Details
    private PlanDetailsDto planDetails;
    private List<ClaimHistoryDto> customerClaimHistory;
    private PlanSummaryDto planSummary;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlanDetailsDto {
        private String planName;
        private Double coverageAmount;
        private Double premiumAmount;
        private String premiumType;
        private Integer duration;
        private String termsAndConditions;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ClaimHistoryDto {
        private String claimNumber;
        private String planName;
        private Double coverageAmount;
        private Double claimedAmount;
        private String claimStatus;
        private String claimDate;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlanSummaryDto {
        private Integer totalPreviousClaims;
        private Double totalPreviousClaimAmount;
        private Double remainingCoverage;
    }
}