package org.springboot.insurancemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OtpResponseDto {
    private boolean success;
    private String message;
}