package org.springboot.insurancemanagementsystem.dto;

import java.time.LocalDate;

public class CustomerResponseDto {
    private Long id;
    private Long userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String city;
    private String state;
    private String nomineeName;
}
