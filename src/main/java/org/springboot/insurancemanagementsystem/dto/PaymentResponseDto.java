package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private Long id;
    private String policyNumber;
    private Double amount;
    private String paymentMode;
    private String status;
    private String transactionReference;
}
