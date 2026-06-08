package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimRequestDto {

    @NotNull(message = "Policy id is required")
    private Long policyId;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be greater than zero")
    private Double claimAmount;

    @NotBlank(message = "Claim reason is required")
    @Size(max = 1000)
    private String claimReason;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(
            message = "Incident date cannot be a future date")
    private LocalDate incidentDate;

    @NotNull(message = "Documents list must not be null")
    @Size(min = 1, message = "At least one document reference required")
    @Valid
    private List<ClaimDocumentRequest> documents;
}
