package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDto {
    private Long customerId;
    private Long userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String city;
    private String state;
    private String nomineeName;
}
