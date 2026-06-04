package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequestDto {
    private Long customerId;
    private Long planId;
    private LocalDate startDate;
}
