package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequestDto {

    @NotNull(message = "Customer id is required")
    @Min(value = 1, message = "Customer id must be a positive number greater than 0")
    private Long customerId;

    @NotNull(message = "Plan id is required")
    @Min(value = 1, message = "Plan id must be a positive number greater than 0")
    private Long planId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    /** Customer-chosen coverage amount; must be within the plan's min–max range. */
    @NotNull(message = "Selected coverage amount is required")
    @Positive(message = "Selected coverage amount must be greater than zero")
    @Max(value = 999999999, message = "Selected coverage amount exceeds the allowable limit")
    private Double selectedCoverageAmount;

    /** Customer-chosen policy duration in months; must be within the plan's min–max range. */
    @NotNull(message = "Selected duration is required")
    @Min(value = 1, message = "Selected duration must be at least 1 month")
    @Max(value = 120, message = "Selected duration cannot exceed 120 months")
    private Integer selectedDuration;

    /** Customer-chosen premium payment cycle. */
    @NotBlank(message = "Premium cycle is required")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Premium type must be one of: MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL"
    )
    private String selectedPremiumType;
}