package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.PolicyPlanRequestDto;
import org.springboot.insurancemanagementsystem.dto.PolicyPlanResponseDto;
import org.springboot.insurancemanagementsystem.entitie.InsuranceProduct;
import org.springboot.insurancemanagementsystem.entitie.PolicyPlan;
import org.springboot.insurancemanagementsystem.enums.PremiumType;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.InsuranceProductRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyPlanRepository;
import org.springboot.insurancemanagementsystem.service.PolicyPlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyPlanServiceImpl
        implements PolicyPlanService {

    private final PolicyPlanRepository planRepository;
    private final InsuranceProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public PolicyPlanResponseDto createPlan(
            PolicyPlanRequestDto request) {

        InsuranceProduct product =
                productRepository.findById(
                                request.getProductId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"));

        if (!product.isActive()) {
            throw new BusinessException(
                    "Cannot create plan for inactive product");
        }

        validatePlan(request);

        PolicyPlan plan = new PolicyPlan();

        plan.setProduct(product);
        plan.setPlanName(request.getPlanName());
        plan.setCoverageAmount(request.getCoverageAmount());
        plan.setPremiumAmount(request.getPremiumAmount());
        plan.setPremiumType(PremiumType.valueOf(request.getPremiumType()));
        plan.setDuration(request.getDuration());
        plan.setTermsAndConditions(
                request.getTermsAndConditions());
        plan.setActive(request.getActive());

        PolicyPlan savedPlan =
                planRepository.save(plan);

        return mapToResponseDto(savedPlan);
    }

    @Override
    public PolicyPlanResponseDto updatePlan(
            Long planId,
            PolicyPlanRequestDto request) {

        PolicyPlan plan =
                planRepository.findById(planId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan not found"));

        InsuranceProduct product =
                productRepository.findById(
                                request.getProductId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"));

        validatePlan(request);

        plan.setProduct(product);
        plan.setPlanName(request.getPlanName());
        plan.setCoverageAmount(request.getCoverageAmount());
        plan.setPremiumAmount(request.getPremiumAmount());
        plan.setPremiumType(PremiumType.valueOf(request.getPremiumType()));
        plan.setDuration(request.getDuration());
        plan.setTermsAndConditions(
                request.getTermsAndConditions());
        plan.setActive(request.getActive());

        PolicyPlan updatedPlan =
                planRepository.save(plan);

        return mapToResponseDto(updatedPlan);
    }

    @Override
    public PolicyPlanResponseDto getPlanById(Long planId) {

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plan not found"));

        return mapToResponseDto(plan);
    }

    @Override
    public List<PolicyPlanResponseDto> getPlansByProduct(
            Long productId) {

        return planRepository
                .findByProduct_IdAndActiveTrue(
                        productId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PolicyPlanResponseDto> getAllPlans(
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

        Page<PolicyPlan> policyPlans = planRepository
                .findAll(pageable);

        return policyPlans.map(this::mapToResponseDto);
    }

    @Override
    public void deactivatePlan(Long planId) {

        PolicyPlan plan =
                planRepository.findById(planId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan not found"));

        plan.setActive(false);

        planRepository.save(plan);
    }

    private void validatePlan(PolicyPlanRequestDto request) {

        if (request.getCoverageAmount() == null
                || request.getCoverageAmount() <= 0) {
            throw new BusinessException(
                    "Coverage amount must be greater than zero");
        }

        if (request.getPremiumAmount() == null
                || request.getPremiumAmount() <= 0) {
            throw new BusinessException(
                    "Premium amount must be greater than zero");
        }

        if (request.getCoverageAmount()
                <= request.getPremiumAmount()) {
            throw new BusinessException(
                    "Coverage amount must be greater than premium amount");
        }

        if (request.getDuration() == null
                || request.getDuration() <= 0) {
            throw new BusinessException(
                    "Duration must be greater than zero");
        }
    }

    private PolicyPlanResponseDto mapToResponseDto(PolicyPlan plan) {

        PolicyPlanResponseDto dto =
                modelMapper.map(plan, PolicyPlanResponseDto.class);

        dto.setProductName(
                plan.getProduct().getProductName());

        dto.setPremiumType(
                plan.getPremiumType().name());

        return dto;
    }
}
