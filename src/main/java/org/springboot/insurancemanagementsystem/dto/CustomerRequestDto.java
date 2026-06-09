package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequestDto {

    @NotBlank(message = "Nominee name is required")
    @Size(min = 2, max = 50, message = "Nominee name must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z\\s]+$",
            message = "Nominee name must contain only letters and spaces only"
    )
    private String nomineeName;

    @NotBlank(message = "Nominee relation is required")
    @Pattern(
            regexp = "^[a-zA-Z\\s]+$",
            message = "Nominee relation must contain letters and spaces only"
    )
    private String nomineeRelation;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    @Size(max = 150, message = "Address cannot exceed 150 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Pattern(
            regexp = "^[a-zA-Z\\s]+$",
            message = "City must contain only letters and spaces"
    )
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(
            regexp = "^[a-zA-Z\\s]+$",
            message = "State must contain only letters and spaces"
    )
    private String state;

    @NotBlank(message = "Pin code is required")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Invalid pin code"
    )
    private String pinCode;
}