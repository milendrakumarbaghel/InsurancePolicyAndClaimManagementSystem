package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    private String fullName;
    private String email;
    private String password;
    private String mobileNumber;
    private String role;
}
