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
    @Min(value = 1, message = "Policy id must be a positive number greater than 0")
    private Long policyId;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be greater than zero")
    @Max(value = 99999999, message = "Claim amount exceeds the maximum allowable processing limit")
    private Double claimAmount;

    @NotBlank(message = "Claim reason is required")
    @Size(min = 10, max = 1000, message = "Claim reason must be between 10 and 1000 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Claim reason cannot contain HTML tags or script character sequences (< or >)"
    )
    private String claimReason;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be a future date")
    private LocalDate incidentDate;

    @NotNull(message = "Documents list must not be null")
    @Size(min = 1, max = 10, message = "You must provide between 1 and 10 supporting documents")
    @Valid
    private List<ClaimDocumentRequest> documents;
}