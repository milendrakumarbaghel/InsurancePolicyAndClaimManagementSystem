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

    // ── Coverage range ──────────────────────────────────────────────────────────

    @NotNull(message = "Maximum coverage amount is required")
    @Positive(message = "Maximum coverage amount must be greater than zero")
    @Max(value = 999999999, message = "Maximum coverage amount exceeds the allowable limit")
    private Double maxCoverageAmount;

    @NotNull(message = "Minimum coverage amount is required")
    @Positive(message = "Minimum coverage amount must be greater than zero")
    @Max(value = 999999999, message = "Minimum coverage amount exceeds the allowable limit")
    private Double minCoverageAmount;

    // ── Premium cycle ────────────────────────────────────────────────────────────

    @NotBlank(message = "Premium type is required")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Premium type must contain only uppercase letters or underscores (e.g., MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL)"
    )
    private String premiumType;

    // ── Duration range ───────────────────────────────────────────────────────────

    @NotNull(message = "Maximum duration is required")
    @Min(value = 1, message = "Maximum duration must be at least 1 month")
    @Max(value = 120, message = "Maximum duration cannot exceed 120 months")
    private Integer maxDuration;

    @NotNull(message = "Minimum duration is required")
    @Min(value = 1, message = "Minimum duration must be at least 1 month")
    @Max(value = 120, message = "Minimum duration cannot exceed 120 months")
    private Integer minDuration;

    // ── Other ────────────────────────────────────────────────────────────────────

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
