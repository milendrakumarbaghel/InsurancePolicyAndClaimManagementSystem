package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.insurancemanagementsystem.dto.CustomerRequestDto;
import org.springboot.insurancemanagementsystem.dto.CustomerResponseDto;
import org.springboot.insurancemanagementsystem.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> createProfile(
            @Valid @RequestBody CustomerRequestDto request,
            Authentication authentication) {

        CustomerResponseDto response =
                customerService.createProfile(
                        request,
                        authentication.getName());

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> updateProfile(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequestDto request,
            Authentication authentication) {

        CustomerResponseDto response =
                customerService.updateProfile(
                        customerId,
                        request,
                        authentication.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> getMyProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                customerService.getMyProfile(
                        authentication.getName()));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<CustomerResponseDto> getCustomerById(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerService.getCustomerById(
                        customerId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Page<CustomerResponseDto>> getAllCustomers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        return ResponseEntity.ok(
                customerService.getAllCustomers(
                        page,
                        size,
                        sortBy,
                        sortDir));
    }
}