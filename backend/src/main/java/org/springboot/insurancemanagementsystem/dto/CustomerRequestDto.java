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
public class CustomerRequestDto {

    @Valid
    @Size(max = 3, message = "A maximum of 3 nominees is allowed.")
    private List<NomineeDto> nominees = new java.util.ArrayList<>();

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;
    @AssertTrue(message = "User must be at least 18 years old")
    public boolean isAdult() {
        if (dateOfBirth == null) {
            return true;
        }
        return !dateOfBirth.plusYears(18).isAfter(LocalDate.now());
    }

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