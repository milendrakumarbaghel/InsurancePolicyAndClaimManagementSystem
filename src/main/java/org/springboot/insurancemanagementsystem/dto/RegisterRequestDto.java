package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDto {

    private String fullName;
    private String email;
    private String password;
    private Long mobile;
}
