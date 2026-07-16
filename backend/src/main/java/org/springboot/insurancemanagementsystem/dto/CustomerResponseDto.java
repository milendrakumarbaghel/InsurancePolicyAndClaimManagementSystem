package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDto {
    private Long customerId;
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String city;
    private String state;
    private String email;
    private String mobileNumber;
    private String address;
    private String pinCode;
    private List<NomineeDto> nominees = new java.util.ArrayList<>();
}
