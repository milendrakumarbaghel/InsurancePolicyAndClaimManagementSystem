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
import org.springboot.insurancemanagementsystem.enums.PremiumType;
import org.springboot.insurancemanagementsystem.enums.ProductType;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.CustomerRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyPlanRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyRepository;
import org.springboot.insurancemanagementsystem.service.PolicyService;
import org.springboot.insurancemanagementsystem.service.PremiumCalculator;
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
    private final PremiumCalculator premiumCalculator;
    private final ModelMapper modelMapper;

    // ── Purchase (customer self-service) ─────────────────────────────────────────

    @Override
    @Transactional
    public PolicyResponseDto purchasePolicy(Long planId,
                                            String customerEmail,
                                            Double selectedCoverageAmount,
                                            Integer selectedDuration,
                                            String selectedPremiumType) {

        log.info("Processing policy purchase. planId={}, customerEmail={}", planId, customerEmail);

        Customer customer = customerRepository.findByUserEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        if (!plan.isActive()) {
            throw new BusinessException("Selected plan is currently inactive.");
        }

        validateSelections(plan, selectedCoverageAmount, selectedDuration);
        validatePurchaseLimits(customer, plan);

        PremiumType premType = parsePremiumType(selectedPremiumType);

        double calculatedPremium = premiumCalculator.calculate(
                selectedCoverageAmount, selectedDuration, premType);

        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .customer(customer)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(selectedDuration))
                .selectedCoverageAmount(selectedCoverageAmount)
                .selectedDuration(selectedDuration)
                .selectedPremiumType(premType)
                .calculatedPremiumAmount(calculatedPremium)
                .totalPremiumPaid(0.0)
                .status(PolicyStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy {} created successfully for {}. PremiumType={}, Premium={}",
                savedPolicy.getPolicyNumber(), customerEmail, premType, calculatedPremium);

        return mapToResponseDto(savedPolicy);
    }

    // ── Issue (admin / officer) ───────────────────────────────────────────────────

    @Override
    @Transactional
    public PolicyResponseDto issuePolicy(PolicyRequestDto request) {

        log.info("Manual issuance requested. customerId={}, planId={}",
                request.getCustomerId(), request.getPlanId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        PolicyPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        validateSelections(plan, request.getSelectedCoverageAmount(), request.getSelectedDuration());

        PremiumType premType = parsePremiumType(request.getSelectedPremiumType());

        double calculatedPremium = premiumCalculator.calculate(
                request.getSelectedCoverageAmount(),
                request.getSelectedDuration(),
                premType);

        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .customer(customer)
                .plan(plan)
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusMonths(request.getSelectedDuration()))
                .selectedCoverageAmount(request.getSelectedCoverageAmount())
                .selectedDuration(request.getSelectedDuration())
                .selectedPremiumType(premType)
                .calculatedPremiumAmount(calculatedPremium)
                .totalPremiumPaid(0.0)
                .status(PolicyStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy {} issued manually. PremiumType={}, Premium={}",
                savedPolicy.getPolicyNumber(), premType, calculatedPremium);

        return mapToResponseDto(savedPolicy);
    }

    // ── Read operations ───────────────────────────────────────────────────────────

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
    public Page<PolicyResponseDto> getAllPolicies(int page, int size, String sortBy, String sortDir,
                                                  PolicyStatus status, Long customerId) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

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

    // ── Helper methods ────────────────────────────────────────────────────────────

    /**
     * Validates that the customer's chosen coverage and duration are within the plan's allowed ranges.
     */
    private void validateSelections(PolicyPlan plan,
                                    Double selectedCoverage,
                                    Integer selectedDuration) {

        if (selectedCoverage == null || selectedCoverage <= 0) {
            throw new BusinessException("Selected coverage amount must be greater than zero.");
        }
        if (plan.getMinCoverageAmount() != null && selectedCoverage < plan.getMinCoverageAmount()) {
            throw new BusinessException(
                    "Selected coverage amount (" + selectedCoverage +
                    ") is below the plan minimum (" + plan.getMinCoverageAmount() + ").");
        }
        if (selectedCoverage > plan.getMaxCoverageAmount()) {
            throw new BusinessException(
                    "Selected coverage amount (" + selectedCoverage +
                    ") exceeds the plan maximum (" + plan.getMaxCoverageAmount() + ").");
        }

        if (selectedDuration == null || selectedDuration <= 0) {
            throw new BusinessException("Selected duration must be greater than zero.");
        }
        if (plan.getMinDuration() != null && selectedDuration < plan.getMinDuration()) {
            throw new BusinessException(
                    "Selected duration (" + selectedDuration +
                    ") is below the plan minimum (" + plan.getMinDuration() + " months).");
        }
        if (selectedDuration > plan.getMaxDuration()) {
            throw new BusinessException(
                    "Selected duration (" + selectedDuration +
                    ") exceeds the plan maximum (" + plan.getMaxDuration() + " months).");
        }
    }

    private void validatePurchaseLimits(Customer customer, PolicyPlan plan) {
        ProductType type = plan.getProduct().getProductType();
        long activeCount = policyRepository.countByCustomerAndPlan_Product_ProductTypeAndStatusIn(
                customer, type, List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT));

        boolean canPurchase = switch (type) {
            case HEALTH -> activeCount < 2;
            case MOTOR  -> activeCount < 1;
            case LIFE   -> activeCount < 5;
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
        PolicyResponseDto dto = new PolicyResponseDto();
        dto.setPolicyId(policy.getId());
        dto.setPolicyNumber(policy.getPolicyNumber());
        dto.setStartDate(policy.getStartDate());
        dto.setEndDate(policy.getEndDate());
        dto.setTotalPremiumPaid(policy.getTotalPremiumPaid());
        dto.setSelectedCoverageAmount(policy.getSelectedCoverageAmount());
        dto.setSelectedDuration(policy.getSelectedDuration());
        dto.setCalculatedPremiumAmount(policy.getCalculatedPremiumAmount());

        if (policy.getStatus() != null) {
            dto.setStatus(policy.getStatus().name());
        }

        if (policy.getCustomer() != null && policy.getCustomer().getUser() != null) {
            dto.setCustomerName(policy.getCustomer().getUser().getFullName());
        }

        if (policy.getPlan() != null) {
            PolicyPlan plan = policy.getPlan();
            dto.setPlanName(plan.getPlanName());
            dto.setPlanMaxCoverageAmount(plan.getMaxCoverageAmount());
            dto.setPlanMinCoverageAmount(plan.getMinCoverageAmount());
            dto.setPlanMaxDuration(plan.getMaxDuration());
            dto.setPlanMinDuration(plan.getMinDuration());

            // Use the customer's selected premium type; fall back to plan default
            PremiumType effectivePremType = policy.getSelectedPremiumType() != null
                    ? policy.getSelectedPremiumType()
                    : plan.getPremiumType();

            if (effectivePremType != null) {
                dto.setSelectedPremiumType(effectivePremType.name());
                dto.setPlanPremiumType(effectivePremType.name()); // keeps RecordPaymentPage working
            }

            if (plan.getProduct() != null) {
                dto.setProductType(plan.getProduct().getProductType().name());
            }
        }

        return dto;
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private PremiumType parsePremiumType(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Premium cycle is required");
        }
        try {
            return PremiumType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "Invalid premium cycle '" + value + "'. Allowed: MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL");
        }
    }
}