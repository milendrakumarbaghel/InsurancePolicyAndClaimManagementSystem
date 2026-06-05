package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.PolicyRequestDto;
import org.springboot.insurancemanagementsystem.dto.PolicyResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PolicyService {

    PolicyResponseDto purchasePolicy(
            Long planId,
            String customerEmail);

    PolicyResponseDto issuePolicy(
            PolicyRequestDto request);

    PolicyResponseDto getPolicyById(Long policyId);

    PolicyResponseDto getPolicyByNumber(
            String policyNumber);

    List<PolicyResponseDto> getMyPolicies(
            String customerEmail);

    Page<PolicyResponseDto> getAllPolicies(
            int page,
            int size,
            String sortBy,
            String sortDir);

    PolicyResponseDto cancelPolicy(Long policyId);
}
