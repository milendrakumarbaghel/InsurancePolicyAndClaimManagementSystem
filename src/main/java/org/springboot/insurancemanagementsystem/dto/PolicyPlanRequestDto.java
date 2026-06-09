package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlanRequestDto {

    @NotNull(message = "Product id is required")
    @Min(value = 1, message = "Product id must be a positive number greater than 0")
    private Long productId;

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s'(),.-]+$",
            message = "Plan name contains invalid special characters"
    )
    private String planName;

    @NotNull(message = "Coverage amount is required")
    @Positive(message = "Coverage amount must be greater than zero")
    @Max(value = 999999999, message = "Coverage amount exceeds maximum allowable limit")
    private Double coverageAmount;

    @NotNull(message = "Premium amount is required")
    @Positive(message = "Premium amount must be greater than zero")
    @Max(value = 9999999, message = "Premium amount exceeds maximum allowable limit")
    private Double premiumAmount;

    @NotBlank(message = "Premium type is required")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Premium type must contain only uppercase alphabetic letters or underscores (e.g., MONTHLY, YEARLY)"
    )
    private String premiumType;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month/year")
    @Max(value = 120, message = "Duration cannot exceed 120 periods")
    private Integer duration;

    @NotBlank(message = "Terms and conditions are required")
    @Size(min = 10, max = 2000, message = "Terms and conditions must be between 10 and 2000 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Terms and conditions cannot contain HTML tags or script character sequences (< or >)"
    )
    private String termsAndConditions;

    @NotNull(message = "Active status is required")
    private Boolean active;
}