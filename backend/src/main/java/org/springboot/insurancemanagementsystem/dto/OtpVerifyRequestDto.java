package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Email OTP is required")
    private String emailOtp;

    @NotBlank(message = "Phone OTP is required")
    private String phoneOtp;
}