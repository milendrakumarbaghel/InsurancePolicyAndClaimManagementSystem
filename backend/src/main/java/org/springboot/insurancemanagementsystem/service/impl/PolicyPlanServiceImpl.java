package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyPlanServiceImpl implements PolicyPlanService {

    private final PolicyPlanRepository planRepository;
    private final InsuranceProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public PolicyPlanResponseDto createPlan(PolicyPlanRequestDto request) {

        log.info("Plan creation request received. PlanName={}, ProductId={}",
                request.getPlanName(), request.getProductId());

        InsuranceProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("Product not found while creating plan. ProductId={}", request.getProductId());
                    return new ResourceNotFoundException("Product not found");
                });

        if (!product.isActive()) {
            log.warn("Attempt to create plan for inactive product. ProductId={}", product.getId());
            throw new BusinessException("Cannot create plan for inactive product");
        }

        validatePlan(request);

        PolicyPlan plan = new PolicyPlan();
        plan.setProduct(product);
        plan.setPlanName(request.getPlanName());
        plan.setMaxCoverageAmount(request.getMaxCoverageAmount());
        plan.setMinCoverageAmount(request.getMinCoverageAmount());
        plan.setPremiumType(parsePremiumType(request.getPremiumType()));
        plan.setMaxDuration(request.getMaxDuration());
        plan.setMinDuration(request.getMinDuration());
        plan.setTermsAndConditions(request.getTermsAndConditions());
        plan.setCreatedAt(LocalDateTime.now());
        plan.setActive(request.getActive());

        PolicyPlan savedPlan = planRepository.save(plan);

        log.info("Policy plan created successfully. PlanId={}, PlanName={}",
                savedPlan.getId(), savedPlan.getPlanName());

        return mapToResponseDto(savedPlan);
    }

    @Override
    public PolicyPlanResponseDto updatePlan(Long planId, PolicyPlanRequestDto request) {

        log.info("Plan update request received. PlanId={}", planId);

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> {
                    log.warn("Plan not found for update. PlanId={}", planId);
                    return new ResourceNotFoundException("Plan not found");
                });

        InsuranceProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("Product not found during plan update. ProductId={}", request.getProductId());
                    return new ResourceNotFoundException("Product not found");
                });

        validatePlan(request);

        plan.setProduct(product);
        plan.setPlanName(request.getPlanName());
        plan.setMaxCoverageAmount(request.getMaxCoverageAmount());
        plan.setMinCoverageAmount(request.getMinCoverageAmount());
        plan.setPremiumType(parsePremiumType(request.getPremiumType()));
        plan.setMaxDuration(request.getMaxDuration());
        plan.setMinDuration(request.getMinDuration());
        plan.setTermsAndConditions(request.getTermsAndConditions());
        plan.setActive(request.getActive());
        plan.setUpdatedAt(LocalDateTime.now());

        PolicyPlan updatedPlan = planRepository.save(plan);

        log.info("Policy plan updated successfully. PlanId={}, PlanName={}",
                updatedPlan.getId(), updatedPlan.getPlanName());

        return mapToResponseDto(updatedPlan);
    }

    @Override
    public PolicyPlanResponseDto getPlanById(Long planId) {

        log.info("Fetching policy plan. PlanId={}", planId);

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> {
                    log.warn("Plan not found. PlanId={}", planId);
                    return new ResourceNotFoundException("Plan not found");
                });

        return mapToResponseDto(plan);
    }

    @Override
    public List<PolicyPlanResponseDto> getPlansByProduct(Long productId) {

        log.info("Fetching plans by product. ProductId={}", productId);

        List<PolicyPlanResponseDto> plans = planRepository
                .findByProduct_IdAndActiveTrue(productId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        log.info("Plans fetched successfully. ProductId={}, Count={}", productId, plans.size());

        return plans;
    }

    @Override
    public Page<PolicyPlanResponseDto> getAllPlans(int page, int size, String sortBy, String sortDir) {

        log.info("Fetching all plans. Page={}, Size={}, SortBy={}, SortDir={}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PolicyPlan> policyPlans = planRepository.findAll(pageable);

        log.info("Plans fetched successfully. TotalRecords={}", policyPlans.getTotalElements());

        return policyPlans.map(this::mapToResponseDto);
    }

    @Override
    public void deactivatePlan(Long planId) {

        log.info("Plan deactivation request received. PlanId={}", planId);

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> {
                    log.warn("Plan not found for deactivation. PlanId={}", planId);
                    return new ResourceNotFoundException("Plan not found");
                });

        plan.setActive(false);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);

        log.info("Policy plan deactivated successfully. PlanId={}, PlanName={}",
                plan.getId(), plan.getPlanName());
    }

    @Override
    public void activatePlan(Long planId) {

        log.info("Plan activation request received. PlanId={}", planId);

        PolicyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> {
                    log.warn("Plan not found for activation. PlanId={}", planId);
                    return new ResourceNotFoundException("Plan not found");
                });

        plan.setActive(true);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);

        log.info("Policy plan activated successfully. PlanId={}, PlanName={}",
                plan.getId(), plan.getPlanName());
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private void validatePlan(PolicyPlanRequestDto request) {

        if (request.getMaxCoverageAmount() == null || request.getMaxCoverageAmount() <= 0) {
            log.warn("Invalid max coverage amount: {}", request.getMaxCoverageAmount());
            throw new BusinessException("Maximum coverage amount must be greater than zero");
        }

        if (request.getMinCoverageAmount() == null || request.getMinCoverageAmount() <= 0) {
            log.warn("Invalid min coverage amount: {}", request.getMinCoverageAmount());
            throw new BusinessException("Minimum coverage amount must be greater than zero");
        }

        if (request.getMinCoverageAmount() >= request.getMaxCoverageAmount()) {
            log.warn("Min coverage amount must be less than max. Min={}, Max={}",
                    request.getMinCoverageAmount(), request.getMaxCoverageAmount());
            throw new BusinessException("Minimum coverage amount must be less than maximum coverage amount");
        }

        if (request.getMaxDuration() == null || request.getMaxDuration() <= 0) {
            log.warn("Invalid max duration: {}", request.getMaxDuration());
            throw new BusinessException("Maximum duration must be greater than zero");
        }

        if (request.getMinDuration() == null || request.getMinDuration() <= 0) {
            log.warn("Invalid min duration: {}", request.getMinDuration());
            throw new BusinessException("Minimum duration must be greater than zero");
        }

        if (request.getMinDuration() >= request.getMaxDuration()) {
            log.warn("Min duration must be less than max. Min={}, Max={}",
                    request.getMinDuration(), request.getMaxDuration());
            throw new BusinessException("Minimum duration must be less than maximum duration");
        }

        parsePremiumType(request.getPremiumType());
    }

    private PremiumType parsePremiumType(String premiumType) {

        if (premiumType == null) {
            throw new BusinessException("Premium type is required");
        }

        try {
            return PremiumType.valueOf(premiumType);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid premium type. Allowed values are: "
                    + Arrays.toString(PremiumType.values()));
        }
    }

    private PolicyPlanResponseDto mapToResponseDto(PolicyPlan plan) {

        PolicyPlanResponseDto dto = new PolicyPlanResponseDto();
        dto.setPolicyPlanId(plan.getId());
        dto.setProductName(plan.getProduct().getProductName());
        dto.setPlanName(plan.getPlanName());
        dto.setMaxCoverageAmount(plan.getMaxCoverageAmount());
        dto.setMinCoverageAmount(plan.getMinCoverageAmount());
        dto.setPremiumType(plan.getPremiumType() != null ? plan.getPremiumType().name() : null);
        dto.setMaxDuration(plan.getMaxDuration());
        dto.setMinDuration(plan.getMinDuration());
        dto.setActive(plan.isActive());

        return dto;
    }
}
