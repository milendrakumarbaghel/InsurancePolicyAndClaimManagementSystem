package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.PolicyRequestDto;
import org.springboot.insurancemanagementsystem.dto.PolicyResponseDto;
import org.springboot.insurancemanagementsystem.entitie.Customer;
import org.springboot.insurancemanagementsystem.entitie.Policy;
import org.springboot.insurancemanagementsystem.entitie.PolicyPlan;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.CustomerRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyPlanRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyRepository;
import org.springboot.insurancemanagementsystem.service.PolicyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final PolicyPlanRepository planRepository;
    private final ModelMapper modelMapper;

    @Override
    public PolicyResponseDto purchasePolicy(
            Long planId,
            String customerEmail) {

        log.info("Policy purchase request received for planId={} by customer={}",
                planId, customerEmail);

        Customer customer = customerRepository
                .findByUserEmail(customerEmail)
                .orElseThrow(() -> {
                    log.warn("Customer profile not found for email={}",
                            customerEmail);
                    return new ResourceNotFoundException(
                            "Customer profile not found");
                });

        PolicyPlan plan = planRepository
                .findById(planId)
                .orElseThrow(() -> {
                    log.warn("Policy plan not found with id={}", planId);
                    return new ResourceNotFoundException(
                            "Plan not found");
                });

        if (!plan.isActive()) {
            log.warn("Attempt to purchase inactive plan. planId={}",
                    planId);
            throw new BusinessException(
                    "Selected plan is inactive");
        }

        Policy policy = new Policy();

        String policyNumber = generatePolicyNumber();

        policy.setPolicyNumber(policyNumber);
        policy.setCustomer(customer);
        policy.setPlan(plan);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(
                LocalDate.now()
                        .plusMonths(plan.getDuration()));
        policy.setTotalPremiumPaid(0.0);
        policy.setStatus(PolicyStatus.PENDING_PAYMENT);

        Policy savedPolicy =
                policyRepository.save(policy);

        log.info("Policy created successfully. policyNumber={}, customer={}",
                savedPolicy.getPolicyNumber(),
                customerEmail);

        return mapToResponseDto(savedPolicy);
    }

    @Override
    public PolicyResponseDto issuePolicy(
            PolicyRequestDto request) {

        log.info("Manual policy issuance requested. customerId={}, planId={}",
                request.getCustomerId(),
                request.getPlanId());

        Customer customer =
                customerRepository.findById(
                                request.getCustomerId())
                        .orElseThrow(() -> {
                            log.warn("Customer not found. customerId={}",
                                    request.getCustomerId());
                            return new ResourceNotFoundException(
                                    "Customer not found");
                        });

        PolicyPlan plan =
                planRepository.findById(
                                request.getPlanId())
                        .orElseThrow(() -> {
                            log.warn("Plan not found. planId={}",
                                    request.getPlanId());
                            return new ResourceNotFoundException(
                                    "Plan not found");
                        });

        Policy policy = new Policy();

        policy.setPolicyNumber(
                generatePolicyNumber());

        policy.setCustomer(customer);
        policy.setPlan(plan);

        policy.setStartDate(
                request.getStartDate());

        policy.setUpdatedAt(
                LocalDateTime.now());

        policy.setEndDate(
                request.getStartDate()
                        .plusMonths(plan.getDuration()));

        policy.setTotalPremiumPaid(0.0);

        policy.setStatus(
                PolicyStatus.PENDING_PAYMENT);

        Policy savedPolicy =
                policyRepository.save(policy);

        log.info("Policy issued successfully. policyNumber={}",
                savedPolicy.getPolicyNumber());

        return mapToResponseDto(savedPolicy);
    }

    @Override
    public PolicyResponseDto getPolicyById(Long policyId, String email, String role) {
        log.debug("Fetching policy by id={}", policyId);
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> {
                    log.warn("Policy not found. id={}",
                            policyId);
                    return new ResourceNotFoundException(
                            "Policy not found");
                });

        if ("CUSTOMER".equals(role) && !policy.getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("Access denied. You can only view your own policy details.");
        }

        return mapToResponseDto(policy);
    }

    @Override
    public PolicyResponseDto getPolicyByNumber(
            String policyNumber) {

        log.debug("Fetching policy by number={}",
                policyNumber);

        Policy policy =
                policyRepository
                        .findByPolicyNumber(
                                policyNumber)
                        .orElseThrow(() -> {
                            log.warn("Policy not found. number={}",
                                    policyNumber);
                            return new ResourceNotFoundException(
                                    "Policy not found");
                        });

        return mapToResponseDto(policy);
    }

    @Override
    public List<PolicyResponseDto> getMyPolicies(
            String customerEmail) {

        log.debug("Fetching policies for customer={}",
                customerEmail);

        return policyRepository
                .findByCustomerUserEmail(
                        customerEmail)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PolicyResponseDto> getAllPolicies(
            int page,
            int size,
            String sortBy,
            String sortDir,
            PolicyStatus status,
            Long customerId) {

        log.debug(
                "Fetching all policies. page={}, size={}, sortBy={}, sortDir={}, status={}, customerId: {}",
                page, size, sortBy, sortDir, status,customerId);

        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<Policy> policyPage;

        if (customerId != null && status != null) {
            policyPage = policyRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else if (customerId != null) {
            policyPage = policyRepository.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            policyPage = policyRepository.findByStatus(status, pageable);
        } else {
            policyPage = policyRepository.findAll(pageable);
        }

        return policyPage.map(this::mapToResponseDto);
    }

    @Override
    public PolicyResponseDto cancelPolicy(
            Long policyId) {

        log.info("Policy cancellation requested. policyId={}",
                policyId);

        Policy policy =
                policyRepository.findById(policyId)
                        .orElseThrow(() -> {
                            log.warn("Policy not found. id={}",
                                    policyId);
                            return new ResourceNotFoundException(
                                    "Policy not found");
                        });

        if (policy.getStatus()
                == PolicyStatus.CANCELLED) {

            log.warn("Policy already cancelled. policyNumber={}",
                    policy.getPolicyNumber());

            throw new BusinessException(
                    "Policy already cancelled");
        }

        policy.setStatus(
                PolicyStatus.CANCELLED);

        Policy updatedPolicy =
                policyRepository.save(policy);

        log.info("Policy cancelled successfully. policyNumber={}",
                updatedPolicy.getPolicyNumber());

        return mapToResponseDto(updatedPolicy);
    }

    private String generatePolicyNumber() {

        String policyNumber =
                "POL-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        log.debug("Generated policy number={}",
                policyNumber);

        return policyNumber;
    }

    private PolicyResponseDto mapToResponseDto(
            Policy policy) {

        PolicyResponseDto dto =
                modelMapper.map(
                        policy,
                        PolicyResponseDto.class);
        dto.setPolicyId(policy.getId());

        dto.setCustomerName(
                policy.getCustomer()
                        .getUser()
                        .getFullName());

        dto.setPlanName(
                policy.getPlan()
                        .getPlanName());

        dto.setStatus(
                policy.getStatus()
                        .name());

        return dto;
    }
}