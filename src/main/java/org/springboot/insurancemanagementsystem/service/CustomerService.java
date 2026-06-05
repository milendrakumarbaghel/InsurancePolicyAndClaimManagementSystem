package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.CustomerRequestDto;
import org.springboot.insurancemanagementsystem.dto.CustomerResponseDto;
import org.springframework.data.domain.Page;

public interface CustomerService {

    CustomerResponseDto createProfile(CustomerRequestDto request, String email);

    CustomerResponseDto updateProfile(Long customerId, CustomerRequestDto request, String email);

    CustomerResponseDto getMyProfile(String email);

    CustomerResponseDto getCustomerById(Long customerId);

    Page<CustomerResponseDto> getAllCustomers(int page, int size, String sortBy, String sortDir);
}
