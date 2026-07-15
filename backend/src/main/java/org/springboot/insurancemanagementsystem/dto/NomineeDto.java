package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springboot.insurancemanagementsystem.enums.AllowedRelation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NomineeDto {

    @NotBlank(message = "Nominee name is required.")
    @Size(min = 2, max = 50, message = "Nominee name must be between 2 and 50 characters.")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Nominee name must contain only letters and spaces.")
    private String name;

    @NotNull(message = "Nominee relation is required.")
    private AllowedRelation relation;
}