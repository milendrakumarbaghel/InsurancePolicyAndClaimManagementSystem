package org.springboot.insurancemanagementsystem.dto;

import java.time.LocalDate;

public class PolicyResponseDto {
    private Long id;
    private String policyNumber;
    private String customerName;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double totalPremiumPaid;
}
