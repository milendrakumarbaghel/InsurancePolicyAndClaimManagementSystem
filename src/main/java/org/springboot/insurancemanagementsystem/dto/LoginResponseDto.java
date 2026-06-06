package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String token;
    private String tokenType;
    private String role;
    private String email;
    private Long expiresIn;
}