package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private Long policyId;
    private Double amount;
    private String paymentMode;
}
