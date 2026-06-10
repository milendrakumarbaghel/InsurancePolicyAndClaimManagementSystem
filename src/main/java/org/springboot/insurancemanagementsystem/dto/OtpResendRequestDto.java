package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpResendRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}