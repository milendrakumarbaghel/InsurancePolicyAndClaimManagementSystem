package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final PolicyPlanRepository planRepository;
    private final ModelMapper modelMapper;

    @Override
    public PolicyResponseDto purchasePolicy(
            Long planId,
            String customerEmail) {

        Customer customer = customerRepository
                .findByUserEmail(customerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer profile not found"));

        PolicyPlan plan = planRepository
                .findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found"));

        if (!plan.isActive()) {
            throw new BusinessException(
                    "Selected plan is inactive");
        }

        Policy policy = new Policy();

        policy.setPolicyNumber(generatePolicyNumber());

        policy.setCustomer(customer);

        policy.setPlan(plan);

        policy.setStartDate(LocalDate.now());

        policy.setEndDate(
                LocalDate.now()
                        .plusMonths(plan.getDuration()));

        policy.setTotalPremiumPaid(0.0);

        policy.setStatus(
                PolicyStatus.PENDING_PAYMENT);

        Policy savedPolicy =
                policyRepository.save(policy);

        return modelMapper.map(savedPolicy, PolicyResponseDto.class);
    }

    @Override
    public PolicyResponseDto issuePolicy(
            PolicyRequestDto request) {

        Customer customer =
                customerRepository.findById(
                                request.getCustomerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"));

        PolicyPlan plan =
                planRepository.findById(
                                request.getPlanId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan not found"));

        Policy policy = new Policy();

        policy.setPolicyNumber(
                generatePolicyNumber());

        policy.setCustomer(customer);

        policy.setPlan(plan);

        policy.setStartDate(
                request.getStartDate());

        policy.setEndDate(
                request.getStartDate()
                        .plusMonths(plan.getDuration()));


        policy.setTotalPremiumPaid(0.0);

        policy.setStatus(
                PolicyStatus.PENDING_PAYMENT);

        Policy savedPolicy =
                policyRepository.save(policy);

        return modelMapper.map(savedPolicy, PolicyResponseDto.class);
    }

    @Override
    public PolicyResponseDto getPolicyById(
            Long policyId) {

        Policy policy =
                policyRepository.findById(policyId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Policy not found"));

        return modelMapper.map(policy, PolicyResponseDto.class);
    }

    @Override
    public PolicyResponseDto getPolicyByNumber(
            String policyNumber) {

        Policy policy =
                policyRepository
                        .findByPolicyNumber(
                                policyNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Policy not found"));

        return modelMapper.map(policy, PolicyResponseDto.class);
    }

    @Override
    public List<PolicyResponseDto> getMyPolicies(
            String customerEmail) {

        return policyRepository
                .findByCustomerUserEmail(
                        customerEmail)
                .stream()
                .map(policy -> modelMapper.map(policy, PolicyResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<PolicyResponseDto> getAllPolicies(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return policyRepository
                .findAll(pageable)
                .map(policy -> modelMapper.map(policy, PolicyResponseDto.class));
    }

    @Override
    public PolicyResponseDto cancelPolicy(
            Long policyId) {

        Policy policy =
                policyRepository.findById(policyId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Policy not found"));

        if (policy.getStatus()
                == PolicyStatus.CANCELLED) {

            throw new BusinessException(
                    "Policy already cancelled");
        }

        policy.setStatus(
                PolicyStatus.CANCELLED);

        Policy updatedPolicy =
                policyRepository.save(policy);

        return modelMapper.map(updatedPolicy, PolicyResponseDto.class);
    }

    private String generatePolicyNumber() {

        return "POL-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
