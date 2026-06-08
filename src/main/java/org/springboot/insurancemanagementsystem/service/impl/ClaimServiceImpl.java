package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.ClaimRequestDto;
import org.springboot.insurancemanagementsystem.dto.ClaimResponseDto;
import org.springboot.insurancemanagementsystem.dto.ClaimReviewRequestDto;
import org.springboot.insurancemanagementsystem.entitie.Claim;
import org.springboot.insurancemanagementsystem.entitie.Customer;
import org.springboot.insurancemanagementsystem.entitie.Policy;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.ClaimRepository;
import org.springboot.insurancemanagementsystem.repository.CustomerRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyRepository;
import org.springboot.insurancemanagementsystem.repository.UserRepository;
import org.springboot.insurancemanagementsystem.service.ClaimService;
import org.springboot.insurancemanagementsystem.service.ClaimStatusHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ClaimStatusHistoryService historyService;

    @Override
    public ClaimResponseDto raiseClaim(
            ClaimRequestDto request,
            String customerEmail) {

        log.info("Customer {} attempting to raise claim for policyId={}",
                customerEmail,
                request.getPolicyId());

        Customer customer =
                customerRepository.findByUserEmail(customerEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"));

        Policy policy =
                policyRepository.findById(request.getPolicyId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Policy not found"));

        if (!policy.getCustomer()
                .getId()
                .equals(customer.getId())) {

            log.warn("Claim rejected. Policy {} does not belong to customer {}",
                    policy.getId(),
                    customerEmail);

            throw new BusinessException(
                    "Policy does not belong to customer");
        }

        if (policy.getStatus() != PolicyStatus.ACTIVE) {

            log.warn("Claim rejected. Policy {} is not ACTIVE",
                    policy.getPolicyNumber());

            throw new BusinessException(
                    "Claim can be raised only on active policy");
        }

        if (request.getClaimAmount()
                .compareTo(policy.getPlan()
                        .getCoverageAmount()) > 0) {

            log.warn("Claim amount {} exceeds coverage amount {}",
                    request.getClaimAmount(),
                    policy.getPlan().getCoverageAmount());

            throw new BusinessException(
                    "Claim amount exceeds coverage");
        }

        Claim claim = new Claim();

        claim.setClaimNumber(generateClaimNumber());
        claim.setPolicy(policy);
        claim.setClaimAmount(request.getClaimAmount());
        claim.setClaimReason(request.getClaimReason());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        claim.setCreatedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());

        Claim savedClaim =
                claimRepository.save(claim);

        log.info("Claim {} raised successfully by customer {}",
                savedClaim.getClaimNumber(),
                customerEmail);

        return mapToResponseDto(savedClaim);
    }

    @Override
    public ClaimResponseDto reviewClaim(
            Long claimId,
            ClaimReviewRequestDto request,
            String agentEmail) {

        log.info("Agent {} reviewing claimId={}",
                agentEmail,
                claimId);

        Claim claim =
                getClaimEntity(claimId);

        claim.setAgentRemarks(request.getRemarks());
        claim.setUpdatedAt(LocalDateTime.now());

        User agent =
                userRepository.findByEmail(agentEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Agent not found"));

        validateFinalStatus(claim);

        ClaimStatus oldStatus =
                claim.getClaimStatus();

        if (request.getRecommended()) {

            claim.setClaimStatus(
                    ClaimStatus.RECOMMENDED_APPROVAL);

        } else {

            claim.setClaimStatus(
                    ClaimStatus.RECOMMENDED_REJECTION);
        }

        Claim updatedClaim =
                claimRepository.save(claim);

        historyService.recordStatusChange(
                claim,
                oldStatus,
                updatedClaim.getClaimStatus(),
                request.getRemarks(),
                agent);

        log.info("Claim {} reviewed by agent {}. Status changed from {} to {}",
                updatedClaim.getClaimNumber(),
                agentEmail,
                oldStatus,
                updatedClaim.getClaimStatus());

        return mapToResponseDto(updatedClaim);
    }

    @Override
    public ClaimResponseDto approveClaim(
            Long claimId,
            String remarks,
            String adminEmail) {

        log.info("Admin {} approving claimId={}",
                adminEmail,
                claimId);

        Claim claim =
                getClaimEntity(claimId);

        claim.setAgentRemarks(remarks);

        User admin =
                userRepository.findByEmail(adminEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Admin not found"));

        if (claim.getClaimStatus()
                != ClaimStatus.RECOMMENDED_APPROVAL) {

            log.warn("Claim {} cannot be approved. Current status={}",
                    claim.getClaimNumber(),
                    claim.getClaimStatus());

            throw new BusinessException(
                    "Claim not recommended for approval");
        }

        ClaimStatus oldStatus =
                claim.getClaimStatus();

        claim.setClaimStatus(
                ClaimStatus.APPROVED);
        claim.setUpdatedAt(LocalDateTime.now());

        Claim updatedClaim =
                claimRepository.save(claim);

        historyService.recordStatusChange(
                claim,
                oldStatus,
                ClaimStatus.APPROVED,
                remarks,
                admin);

        log.info("Claim {} approved by admin {}",
                updatedClaim.getClaimNumber(),
                adminEmail);

        return mapToResponseDto(updatedClaim);
    }

    @Override
    public ClaimResponseDto rejectClaim(
            Long claimId,
            String remarks,
            String adminEmail) {

        log.info("Admin {} rejecting claimId={}",
                adminEmail,
                claimId);

        Claim claim =
                getClaimEntity(claimId);

        User admin =
                userRepository.findByEmail(adminEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Admin not found"));

        validateFinalStatus(claim);

        ClaimStatus oldStatus =
                claim.getClaimStatus();

        claim.setClaimStatus(
                ClaimStatus.REJECTED);
        claim.setUpdatedAt(LocalDateTime.now());

        Claim updatedClaim =
                claimRepository.save(claim);

        historyService.recordStatusChange(
                claim,
                oldStatus,
                ClaimStatus.REJECTED,
                remarks,
                admin);

        log.info("Claim {} rejected by admin {}",
                updatedClaim.getClaimNumber(),
                adminEmail);

        return mapToResponseDto(updatedClaim);
    }

    @Override
    public ClaimResponseDto getClaimById(
            Long claimId) {

        log.debug("Fetching claim by id={}", claimId);

        return mapToResponseDto(
                getClaimEntity(claimId));
    }

    @Override
    public ClaimResponseDto getClaimByNumber(
            String claimNumber) {

        log.debug("Fetching claim by number={}",
                claimNumber);

        Claim claim =
                claimRepository.findByClaimNumber(claimNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Claim not found"));

        return mapToResponseDto(claim);
    }

    @Override
    public List<ClaimResponseDto> getMyClaims(
            String customerEmail) {

        log.debug("Fetching claims for customer={}",
                customerEmail);

        return claimRepository
                .findByPolicyCustomerUserEmail(customerEmail)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ClaimResponseDto> getAllClaims(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.debug("Fetching all claims page={}, size={}",
                page,
                size);

        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return claimRepository
                .findAll(pageable)
                .map(this::mapToResponseDto);
    }

    private Claim getClaimEntity(
            Long claimId) {

        return claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim not found"));
    }

    private void validateFinalStatus(
            Claim claim) {

        if (claim.getClaimStatus()
                == ClaimStatus.APPROVED
                || claim.getClaimStatus()
                == ClaimStatus.REJECTED) {

            log.warn("Modification attempted on finalized claim {}",
                    claim.getClaimNumber());

            throw new BusinessException(
                    "Approved or rejected claim cannot be modified");
        }
    }

    private String generateClaimNumber() {

        return "CLM-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private ClaimResponseDto mapToResponseDto(Claim claim) {

        ClaimResponseDto dto =
                modelMapper.map(
                        claim,
                        ClaimResponseDto.class);

        if (claim.getPolicy() != null) {

            dto.setPolicyId(
                    claim.getPolicy().getId());

            dto.setPolicyNumber(
                    claim.getPolicy().getPolicyNumber());

            if (claim.getPolicy().getCustomer() != null
                    && claim.getPolicy().getCustomer().getUser() != null) {

                dto.setCustomerName(
                        claim.getPolicy()
                                .getCustomer()
                                .getUser()
                                .getFullName());
            }
        }

        if (claim.getClaimStatus() != null) {
            dto.setClaimStatus(
                    claim.getClaimStatus().name());
        }

        dto.setAgentRemarks(
                claim.getAgentRemarks());

        dto.setAdminRemarks(
                claim.getAdminRemarks());

        return dto;
    }
}