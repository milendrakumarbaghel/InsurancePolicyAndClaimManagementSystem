package org.springboot.insurancemanagementsystem.dto;

import java.time.LocalDate;

public class ClaimRequestDto {
    private Long policyId;
    private Double claimAmount;
    private String claimReason;
    private LocalDate incidentDate;
}
