package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.CustomerRequestDto;
import org.springboot.insurancemanagementsystem.dto.CustomerResponseDto;
import org.springboot.insurancemanagementsystem.entitie.Customer;
import org.springboot.insurancemanagementsystem.entitie.User;
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

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public CustomerResponseDto createProfile(
            CustomerRequestDto request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        // Check if a customer profile already exists for this user
        if (customerRepository.existsByUserId(user.getId())) {
            throw new BusinessException(
                    "Customer profile already exists");
        }

        Customer customer = new Customer();

        customer.setUser(user);

        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPinCode(request.getPinCode());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setNomineeName(request.getNomineeName());
        customer.setNomineeRelation(request.getNomineeRelation());

        Customer savedCustomer =
                customerRepository.save(customer);

        return mapToResponseDto(savedCustomer);
    }

    @Override
    public CustomerResponseDto updateProfile(
            Long customerId,
            CustomerRequestDto request,
            String email) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found"));

        if (!customer.getUser().getEmail().equals(email)) {
            throw new BusinessException(
                    "You are not allowed to update another customer's profile");
        }

        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPinCode(request.getPinCode());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setNomineeName(request.getNomineeName());
        customer.setNomineeRelation(request.getNomineeRelation());

        Customer updatedCustomer =
                customerRepository.save(customer);

        return mapToResponseDto(updatedCustomer);
    }

    @Override
    public CustomerResponseDto getMyProfile(String email) {

        Customer customer = customerRepository
                .findByUserEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer profile not found"));

        return mapToResponseDto(customer);
    }

    @Override
    public CustomerResponseDto getCustomerById(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found"));

        return mapToResponseDto(customer);
    }

    @Override
    public Page<CustomerResponseDto> getAllCustomers(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<Customer> customers = customerRepository.findAll(pageable);

        return customers.map(this::mapToResponseDto);
    }

    private CustomerResponseDto mapToResponseDto(Customer customer) {

        CustomerResponseDto dto =
                modelMapper.map(customer, CustomerResponseDto.class);

        dto.setUserId(customer.getUser().getId());
        dto.setFullName(customer.getUser().getFullName());

        return dto;
    }
}
