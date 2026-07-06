package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimAssignRequestDto {

    @NotNull(message = "Insurance Operations Officer ID is required")
    @Min(value = 1, message = "Insurance Operations Officer ID must be a positive number")
    private Long insuranceOperationsOfficerId;
}
