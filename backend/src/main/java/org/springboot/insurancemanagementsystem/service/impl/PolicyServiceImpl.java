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
import org.springboot.insurancemanagementsystem.enums.ProductType;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.CustomerRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyPlanRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyRepository;
import org.springboot.insurancemanagementsystem.service.PolicyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public PolicyResponseDto purchasePolicy(Long planId, String customerEmail) {
        log.info("Processing policy purchase. planId={}, customerEmail={}", planId, customerEmail);

        Customer customer = customerRepository.findByUserEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        if (!plan.isActive()) {
            throw new BusinessException("Selected plan is currently inactive.");
        }

        validatePurchaseLimits(customer, plan);

        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .customer(customer)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(plan.getDuration()))
                .totalPremiumPaid(0.0)
                .status(PolicyStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy {} created successfully for {}", savedPolicy.getPolicyNumber(), customerEmail);

        return mapToResponseDto(savedPolicy);
    }

    @Override
    @Transactional
    public PolicyResponseDto issuePolicy(PolicyRequestDto request) {
        log.info("Manual issuance requested. customerId={}, planId={}", request.getCustomerId(), request.getPlanId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        PolicyPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .customer(customer)
                .plan(plan)
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusMonths(plan.getDuration()))
                .totalPremiumPaid(0.0)
                .status(PolicyStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy {} issued manually.", savedPolicy.getPolicyNumber());

        return mapToResponseDto(savedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyResponseDto getPolicyById(Long policyId, String email, String role) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        validateCustomerAccess(policy, email, role);
        return mapToResponseDto(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyResponseDto getPolicyByNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
        return mapToResponseDto(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyResponseDto> getMyPolicies(String customerEmail) {
        return policyRepository.findByCustomerUserEmail(customerEmail).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PolicyResponseDto> getAllPolicies(int page, int size, String sortBy, String sortDir, PolicyStatus status, Long customerId) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        // REQUIREMENT: These repository methods MUST use @EntityGraph(attributePaths = {"customer.user", "plan.product"})
        // in PolicyRepository.java to prevent severe N+1 database queries.
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
    @Transactional
    public PolicyResponseDto cancelPolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            throw new BusinessException("Policy is already cancelled");
        }

        policy.setStatus(PolicyStatus.CANCELLED);
        policy.setUpdatedAt(LocalDateTime.now());

        log.info("Policy {} cancelled.", policy.getPolicyNumber());
        return mapToResponseDto(policyRepository.save(policy));
    }

    // --- Helper Methods ---

    private void validatePurchaseLimits(Customer customer, PolicyPlan plan) {
        ProductType type = plan.getProduct().getProductType();
        long activeCount = policyRepository.countByCustomerAndPlan_Product_ProductTypeAndStatusIn(
                customer, type, List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT));

        boolean canPurchase = switch (type) {
            case HEALTH -> activeCount < 2;
            case MOTOR -> activeCount < 1;
            case LIFE -> activeCount < 5;
            case TRAVEL -> activeCount == 0;
        };

        if (!canPurchase) {
            throw new BusinessException("Maximum limit reached for product type: " + type);
        }
    }

    private void validateCustomerAccess(Policy policy, String email, String role) {
        if ("CUSTOMER".equals(role) && !policy.getCustomer().getUser().getEmail().equals(email)) {
            log.warn("Unauthorized access attempt. email={} tried to view policyId={}", email, policy.getId());
            throw new BusinessException("Access denied. You can only view your own policy details.");
        }
    }

    private String generatePolicyNumber() {
        return "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PolicyResponseDto mapToResponseDto(Policy policy) {
        PolicyResponseDto dto = modelMapper.map(policy, PolicyResponseDto.class);
        dto.setPolicyId(policy.getId());

        // Null checks added for safety against bad data mapping
        if (policy.getCustomer() != null && policy.getCustomer().getUser() != null) {
            dto.setCustomerName(policy.getCustomer().getUser().getFullName());
        }
        if (policy.getPlan() != null) {
            dto.setPlanName(policy.getPlan().getPlanName());
            if (policy.getPlan().getProduct() != null) {
                dto.setProductType(policy.getPlan().getProduct().getProductType().name());
            }
        }
        dto.setStatus(policy.getStatus().name());
        return dto;
    }
}