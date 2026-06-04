package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimRequestDto {
    private Long policyId;
    private Double claimAmount;
    private String claimReason;
    private LocalDate incidentDate;
}
