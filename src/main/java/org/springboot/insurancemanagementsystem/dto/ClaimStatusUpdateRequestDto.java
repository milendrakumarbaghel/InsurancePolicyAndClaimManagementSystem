package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimStatusUpdateRequestDto {
    private Long claimId;
    private String status;
    private String remarks;
}
