package org.springboot.insurancemanagementsystem.dto;

import java.time.LocalDate;

public class CustomerRequestDto {
    private Long userId;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String nomineeName;
    private String nomineeRelation;
}
