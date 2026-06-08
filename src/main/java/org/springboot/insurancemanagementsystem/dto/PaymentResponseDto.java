package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private Long paymentId;
    private String policyNumber;
    private Double amount;
    private String paymentMode;
    private String status;
    private String transactionReference;
}
