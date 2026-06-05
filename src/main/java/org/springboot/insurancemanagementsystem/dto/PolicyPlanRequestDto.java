package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlanRequestDto {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotBlank(message = "Plan name is required")
    private String planName;

    @NotNull(message = "Coverage amount is required")
    @Positive(message = "Coverage amount must be greater than zero")
    private Double coverageAmount;

    @NotNull(message = "Premium amount is required")
    @Positive(message = "Premium amount must be greater than zero")
    private Double premiumAmount;

    @NotBlank(message = "Premium type is required")
    private String premiumType;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than zero")
    private Integer duration;

    @NotBlank(message = "Terms and conditions are required")
    @Size(max = 2000)
    private String termsAndConditions;

    @NotNull(message = "Active status is required")
    private Boolean active;
}