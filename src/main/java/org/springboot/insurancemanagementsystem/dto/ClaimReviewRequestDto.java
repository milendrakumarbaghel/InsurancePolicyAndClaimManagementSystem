package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimReviewRequestDto {

    @NotNull(message = "Recommendation status is required (true/false)")
    private Boolean recommended;

    @NotBlank(message = "Review remarks are required")
    @Size(min = 5, max = 500, message = "Remarks must be between 5 and 500 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Remarks cannot contain HTML tags or script character sequences (< or >)"
    )
    private String remarks;
}