package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long userId;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String role;
    private boolean active;
}
