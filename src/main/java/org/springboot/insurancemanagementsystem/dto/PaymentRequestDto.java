package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotNull(message = "Policy Plan id is required")
    @Min(value = 1, message = "Policy Plan id must be a positive number greater than 0")
    private Long policyPlanId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Max(value = 9999999, message = "Amount exceeds maximum allowable transaction limit")
    private Double amount;

    @NotBlank(message = "Policy number is required")
    @Size(min = 5, max = 30, message = "Policy number must be between 5 and 30 characters")
    @Pattern(
            regexp = "^[A-Z0-9\\-]+$",
            message = "Policy number must contain only uppercase alphanumeric characters and hyphens"
    )
    private String policyNumber;

    @NotBlank(message = "Payment mode is required")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Payment mode must be uppercase letters or underscores matching system modes (e.g., CARD, NET_BANKING, UPI)"
    )
    private String paymentMode;
}