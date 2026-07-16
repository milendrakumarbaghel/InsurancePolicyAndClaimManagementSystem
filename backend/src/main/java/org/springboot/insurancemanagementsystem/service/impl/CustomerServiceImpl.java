package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.CustomerRequestDto;
import org.springboot.insurancemanagementsystem.dto.CustomerResponseDto;
import org.springboot.insurancemanagementsystem.dto.NomineeDto;
import org.springboot.insurancemanagementsystem.entitie.Customer;
import org.springboot.insurancemanagementsystem.entitie.Nominee;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.enums.AllowedRelation;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.CustomerRepository;
import org.springboot.insurancemanagementsystem.repository.UserRepository;
import org.springboot.insurancemanagementsystem.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public CustomerResponseDto createProfile(CustomerRequestDto request, String email) {

        log.info("Customer profile creation requested by user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found while creating customer profile: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        if (customerRepository.existsByUserId(user.getId())) {
            log.warn("Customer profile already exists for userId={}, email={}", user.getId(), email);
            throw new BusinessException("Customer profile already exists");
        }

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPinCode(request.getPinCode());
        customer.setCreatedAt(LocalDateTime.now());

        // Initialize list if it wasn't pre-initialized in the Customer entity constructor
        if (customer.getNominees() == null) {
            customer.setNominees(new java.util.ArrayList<>());
        }

        // Process and validate the nominee collection
        if (request.getNominees() != null) {
            if (request.getNominees().size() > 3) {
                throw new BusinessException("A maximum of 3 nominees is allowed.");
            }

            for (NomineeDto dto : request.getNominees()) {
                if (dto.getRelation() == null || !AllowedRelation.isValid(dto.getRelation().name())) {
                    throw new BusinessException("Invalid relation values selected.");
                }

                Nominee nominee = new Nominee();
                nominee.setName(dto.getName());
                nominee.setRelation(dto.getRelation());
                nominee.setCustomer(customer);

                // Establishes the bi-directional relationship so Hibernate cascades the save correctly
                customer.getNominees().add(nominee);
            }
        }

        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer profile created successfully. CustomerId={}, UserId={}",
                savedCustomer.getId(), user.getId());

        return mapToResponseDto(savedCustomer);
    }

    @Override
    public CustomerResponseDto updateProfile(
            Long customerId,
            CustomerRequestDto request,
            String email) {

        log.info(
                "Customer profile update requested. CustomerId={}, Email={}",
                customerId,
                email);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn(
                            "Customer not found while updating profile. CustomerId={}",
                            customerId);

                    return new ResourceNotFoundException(
                            "Customer not found");
                });

        if (!customer.getUser().getEmail().equals(email)) {

            log.warn(
                    "Unauthorized profile update attempt. CustomerId={}, RequestedBy={}",
                    customerId,
                    email);

            throw new BusinessException(
                    "You are not allowed to update another customer's profile");
        }

        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPinCode(request.getPinCode());
        customer.setUpdatedAt(LocalDateTime.now());

        if (request.getNominees() != null) {
            if (request.getNominees().size() > 3) {
                throw new BusinessException("A maximum of 3 nominees is allowed.");
            }

            // Clear the existing nominee references to cleanly handle updates/deletions seamlessly
            customer.getNominees().clear();

            for (NomineeDto dto : request.getNominees()) {
                if (dto.getRelation() == null || !AllowedRelation.isValid(dto.getRelation().name())) {
                    throw new BusinessException("Invalid relation values selected.");
                }

                Nominee nominee = new Nominee();
                nominee.setName(dto.getName());
                nominee.setRelation(dto.getRelation());
                nominee.setCustomer(customer);
                customer.getNominees().add(nominee);
            }
        }

        Customer updatedCustomer =
                customerRepository.save(customer);

        log.info(
                "Customer profile updated successfully. CustomerId={}",
                customerId);

        return mapToResponseDto(updatedCustomer);
    }

    @Override
    public CustomerResponseDto getCustomerByUserId(Long userId) {
        Customer customer = customerRepository
                .findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Customer profile not found for userID: {}", userId);
                    return new ResourceNotFoundException("Customer profile not found");
                });

        return mapToResponseDto(customer);
    }

    @Override
    public CustomerResponseDto getMyProfile(String email) {

        log.info("Fetching customer profile for user: {}", email);

        Customer customer = customerRepository
                .findByUserEmail(email)
                .orElseThrow(() -> {
                    log.warn(
                            "Customer profile not found for email: {}",
                            email);

                    return new ResourceNotFoundException(
                            "Customer profile not found");
                });

        return mapToResponseDto(customer);
    }

    @Override
    public CustomerResponseDto getCustomerById(Long customerId) {

        log.info("Fetching customer details. CustomerId={}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn(
                            "Customer not found. CustomerId={}",
                            customerId);

                    return new ResourceNotFoundException(
                            "Customer not found");
                });

        return mapToResponseDto(customer);
    }

    @Override
    public Page<CustomerResponseDto> getAllCustomers(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search) {

        log.info(
                "Fetching all customers. Page={}, Size={}, SortBy={}, SortDir={}, search: {}",
                page,
                size,
                sortBy,
                sortDir,
                search);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<Customer> customers;

        if (search != null && !search.trim().isEmpty()) {
            customers = customerRepository.searchCustomers(search.trim(), pageable);
        } else {
            customers = customerRepository.findAll(pageable);
        }

        log.info(
                "Customer list fetched successfully. TotalRecords={}",
                customers.getTotalElements());

        return customers.map(this::mapToResponseDto);
    }

    private CustomerResponseDto mapToResponseDto(Customer customer) {
        CustomerResponseDto dto = modelMapper.map(customer, CustomerResponseDto.class);

        dto.setCustomerId(customer.getId());
        dto.setEmail(customer.getUser().getEmail());
        dto.setMobileNumber(customer.getUser().getMobileNumber());
        dto.setUserId(customer.getUser().getId());
        dto.setFirstName(customer.getUser().getFirstName());
        dto.setMiddleName(customer.getUser().getMiddleName());
        dto.setLastName(customer.getUser().getLastName());

        // Map the collection of nominees to NomineeDto list
        if (customer.getNominees() != null) {
            dto.setNominees(customer.getNominees().stream()
                    .map(nominee -> NomineeDto.builder()
                            .name(nominee.getName())
                            .relation(nominee.getRelation())
                            .build())
                    .collect(java.util.stream.Collectors.toList()));
        } else {
            dto.setNominees(new java.util.ArrayList<>());
        }

        return dto;
    }
}