package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponseDto {
    private Long policyId;
    private String policyNumber;
    private String customerName;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double totalPremiumPaid;
    private String productType;
}
