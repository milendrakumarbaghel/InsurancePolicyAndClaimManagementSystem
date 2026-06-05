package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.PolicyPlanRequestDto;
import org.springboot.insurancemanagementsystem.dto.PolicyPlanResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PolicyPlanService {

    PolicyPlanResponseDto createPlan(PolicyPlanRequestDto request);

    PolicyPlanResponseDto updatePlan(
            Long planId,
            PolicyPlanRequestDto request);

    PolicyPlanResponseDto getPlanById(Long planId);

    List<PolicyPlanResponseDto> getPlansByProduct(
            Long productId);

    Page<PolicyPlanResponseDto> getAllPlans(
            int page,
            int size,
            String sortBy,
            String sortDir);

    void deactivatePlan(Long planId);
}