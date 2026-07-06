package org.springboot.insurancemanagementsystem.dto;

import lombok.*;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimStatusHistoryResponse {

    private Long historyId;

    private String claimNumber;

    private ClaimStatus previousStatus;

    private ClaimStatus newStatus;

    private String remarks;

    private String updatedBy;

    private String updatedByRole;

    private LocalDateTime updatedAt;
}