package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminClaimDecisionRequestDto {

    @NotBlank(message = "Remarks are required for the final decision")
    @Size(min = 5, max = 500, message = "Remarks must be between 5 and 500 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Remarks cannot contain HTML tags or script character sequences (< or >)"
    )
    private String remarks;
}
