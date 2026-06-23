package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> createProfile(
            @Valid @RequestBody CustomerRequestDto request,
            Authentication authentication) {

        log.info("Customer profile creation request received for user: {}",
                authentication.getName());

        CustomerResponseDto response =
                customerService.createProfile(
                        request,
                        authentication.getName());

        log.info("Customer profile created successfully. Customer ID: {}",
                response.getCustomerId());

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

        log.info(
                "Customer profile update request received. Customer ID: {}, User: {}",
                customerId,
                authentication.getName());

        CustomerResponseDto response =
                customerService.updateProfile(
                        customerId,
                        request,
                        authentication.getName());

        log.info("Customer profile updated successfully. Customer ID: {}",
                customerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponseDto> getMyProfile(
            Authentication authentication) {

        log.info("Fetching profile for customer: {}",
                authentication.getName());

        CustomerResponseDto response =
                customerService.getMyProfile(
                        authentication.getName());

        log.info("Customer profile retrieved successfully for user: {}",
                authentication.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<CustomerResponseDto> getCustomerById(
            @PathVariable Long customerId) {

        log.info("Fetching customer profile. Customer ID: {}",
                customerId);

        CustomerResponseDto response =
                customerService.getCustomerById(
                        customerId);

        log.info("Customer profile retrieved successfully. Customer ID: {}",
                customerId);

        return ResponseEntity.ok(response);
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
            String sortDir,

            @RequestParam(required = false)
                    String search) {

        log.info(
                "Fetching customers | page: {}, size: {}, sortBy: {}, sortDir: {}, search: {}",
                page,
                size,
                sortBy,
                sortDir,
                search);

        Page<CustomerResponseDto> customers =
                customerService.getAllCustomers(
                        page,
                        size,
                        sortBy,
                        sortDir,
                        search);

        log.info(
                "Retrieved {} customer records",
                customers.getNumberOfElements());

        return ResponseEntity.ok(customers);
    }
}