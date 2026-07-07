package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.*;
import org.springboot.insurancemanagementsystem.entitie.*;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.enums.Role;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.CoverageExhaustedException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.*;
import org.springboot.insurancemanagementsystem.service.ClaimService;
import org.springboot.insurancemanagementsystem.service.ClaimStatusHistoryService;
import org.springboot.insurancemanagementsystem.service.EmailService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ClaimStatusHistoryService historyService;
    private final EmailService emailService;

    @Override
    @Transactional
    public ClaimResponseDto raiseClaim(
            ClaimRequestDto request,
            String customerEmail) {

        log.info("Customer {} attempting to raise claim for policyId={}",
                customerEmail,
                request.getPolicyId());

        Customer customer =
                customerRepository.findByUserEmail(customerEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Customer not found"));

        Policy policy = policyRepository.findByIdWithLock(request.getPolicyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Policy not found with id : " + request.getPolicyId()));

        if (!policy.getCustomer().getId().equals(customer.getId())) {

            log.warn("Claim rejected. Policy {} does not belong to customer {}",
                    policy.getId(),
                    customerEmail);

            throw new BusinessException("Policy does not belong to customer");
        }

        if (policy.getStatus() != PolicyStatus.ACTIVE) {

            log.warn("Claim rejected. Policy {} is not ACTIVE",
                    policy.getPolicyNumber());

            throw new BusinessException("Claim can be raised only on active policy");
        }

        if (request.getClaimAmount()
                .compareTo(policy.getPlan().getCoverageAmount()) > 0) {

            log.warn("Claim amount {} exceeds coverage amount {}",
                    request.getClaimAmount(),
                    policy.getPlan().getCoverageAmount());

            throw new BusinessException("Claim amount exceeds coverage");
        }
        List<ClaimStatus> approvedStatuses = Collections.singletonList(ClaimStatus.APPROVED);
        Double approvedClaimsTotal = claimRepository.getApprovedClaimAmountByPolicyId(policy.getId(), approvedStatuses);
        Double coverageAmount = policy.getPlan().getCoverageAmount();

        Double remainingCoverage = coverageAmount - approvedClaimsTotal;
        if (remainingCoverage < 0) remainingCoverage = 0.0;

        if (request.getClaimAmount() > remainingCoverage) {
            throw new CoverageExhaustedException(
                    "Requested claim amount exceeds the remaining policy coverage.",
                    coverageAmount,
                    approvedClaimsTotal,
                    remainingCoverage,
                    request.getClaimAmount()
            );
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

        Claim savedClaim = claimRepository.save(claim);

        log.info("Claim {} raised successfully by customer {}",
                savedClaim.getClaimNumber(),
                customerEmail);

        List<ClaimDocument> savedDocs = new ArrayList<>();

        if (request.getDocuments() != null) {
            for (ClaimDocumentRequest d : request.getDocuments()) {
                savedDocs.add(
                        claimDocumentRepository.save(
                                ClaimDocument.builder()
                                        .claim(savedClaim)
                                        .documentName(d.getDocumentName())
                                        .documentType(d.getDocumentType())
                                        .documentReference(d.getDocumentReference())
                                        .uploadedDate(LocalDateTime.now())
                                        .build()
                        )
                );
            }
        }

        recordHistory(
                savedClaim,
                null,
                ClaimStatus.SUBMITTED,
                "Claim submitted by customer",
                customer.getUser()
        );

        return toClaimResponse(savedClaim, savedDocs);
    }

    @Override
    @Transactional
    public ClaimResponseDto assignClaimToInsuranceOperationsOfficer(Long claimId, Long insuranceOperationsOfficerId, String adminEmail) {

        log.info("Admin {} assigning claimId={} to InsuranceOperationsOfficerId={}", adminEmail, claimId, insuranceOperationsOfficerId);

        Claim claim = getClaimEntity(claimId);

        // Only allow assignment when claim is SUBMITTED or re-assignment when ASSIGNED
        if (claim.getClaimStatus() != ClaimStatus.SUBMITTED && claim.getClaimStatus() != ClaimStatus.ASSIGNED) {
            log.warn("Cannot assign claim {}. Current status={}", claim.getClaimNumber(), claim.getClaimStatus());
            throw new BusinessException("Claim can only be assigned when in SUBMITTED or ASSIGNED status. Current status: " + claim.getClaimStatus());
        }

        User insuranceOperationsOfficer = userRepository.findById(insuranceOperationsOfficerId)
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceOperationsOfficer not found with id: " + insuranceOperationsOfficerId));

        if (insuranceOperationsOfficer.getRole() != Role.INSURANCE_OPERATIONS_OFFICER) {
            throw new BusinessException("User with id " + insuranceOperationsOfficerId + " is not an agent");
        }

        if (!insuranceOperationsOfficer.isActive()) {
            throw new BusinessException("Agent is not active");
        }

        // Check if the claim is already assigned to the same agent
        if (claim.getAssignedInsuranceOperationsOfficer() != null && claim.getAssignedInsuranceOperationsOfficer().getId().equals(insuranceOperationsOfficerId)) {
            throw new BusinessException("Claim is already assigned to this agent");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        ClaimStatus oldStatus = claim.getClaimStatus();
        claim.setAssignedInsuranceOperationsOfficer(insuranceOperationsOfficer);
        claim.setAssignedAt(LocalDateTime.now());
        claim.setClaimStatus(ClaimStatus.ASSIGNED);
        claim.setUpdatedAt(LocalDateTime.now());

        Claim updatedClaim = claimRepository.save(claim);
        List<ClaimDocument> docs = claimDocumentRepository.findByClaimId(updatedClaim.getId());

        historyService.recordStatusChange(claim, oldStatus, ClaimStatus.ASSIGNED,
                "Claim assigned to agent: " + insuranceOperationsOfficer.getFullName(), admin);

        log.info("Claim {} assigned to agent {} by admin {}",
                updatedClaim.getClaimNumber(), insuranceOperationsOfficer.getFullName(), adminEmail);

        return toEnhancedClaimResponse(updatedClaim, docs);
    }

    @Override
    public ClaimResponseDto reviewClaim(
            Long claimId,
            ClaimReviewRequestDto request,
            String agentEmail) {

        log.info("Agent {} reviewing claimId={}", agentEmail, claimId);

        Claim claim = getClaimEntity(claimId);

        // Validate the claim is in ASSIGNED status
        if (claim.getClaimStatus() != ClaimStatus.ASSIGNED) {
            log.warn("Claim {} cannot be reviewed. Current status={}", claim.getClaimNumber(), claim.getClaimStatus());
            throw new BusinessException("Claim can only be reviewed when in ASSIGNED status. Current status: " + claim.getClaimStatus());
        }

        // Validate only the assigned agent can review
        if (claim.getAssignedInsuranceOperationsOfficer() == null || !claim.getAssignedInsuranceOperationsOfficer().getEmail().equals(agentEmail)) {
            log.warn("Agent {} is not assigned to claim {}", agentEmail, claim.getClaimNumber());
            throw new BusinessException("Only the assigned agent can review this claim");
        }

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        claim.setAgentRemarks(request.getRemarks());
        claim.setUpdatedAt(LocalDateTime.now());

        ClaimStatus oldStatus = claim.getClaimStatus();

        if (request.getRecommended()) {
            claim.setClaimStatus(ClaimStatus.RECOMMENDED_APPROVAL);
        } else {
            claim.setClaimStatus(ClaimStatus.RECOMMENDED_REJECTION);
        }

        Claim updatedClaim = claimRepository.save(claim);
        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(updatedClaim.getId());

        historyService.recordStatusChange(claim, oldStatus, updatedClaim.getClaimStatus(), request.getRemarks(), agent);

        log.info("Claim {} reviewed by agent {}. Status changed from {} to {}",
                updatedClaim.getClaimNumber(), agentEmail, oldStatus, updatedClaim.getClaimStatus());

        return toEnhancedClaimResponse(updatedClaim, byClaimId);
    }

    @Override
    @Transactional
    public ClaimResponseDto approveClaim(
            Long claimId,
            String remarks,
            String adminEmail) {

        log.info("Admin {} approving claimId={}", adminEmail, claimId);

        Claim claim = getClaimEntity(claimId);

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        // Admin can approve from either RECOMMENDED_APPROVAL or RECOMMENDED_REJECTION (override)
        if (claim.getClaimStatus() != ClaimStatus.RECOMMENDED_APPROVAL
                && claim.getClaimStatus() != ClaimStatus.RECOMMENDED_REJECTION) {
            log.warn("Claim {} cannot be approved. Current status={}", claim.getClaimNumber(), claim.getClaimStatus());
            throw new BusinessException("Claim must be reviewed by an agent before admin can approve. Current status: " + claim.getClaimStatus());
        }

        Policy policy = policyRepository.findByIdWithLock(claim.getPolicy().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        List<ClaimStatus> approvedStatuses = Collections.singletonList(ClaimStatus.APPROVED);
        Double approvedClaimsTotal = claimRepository.getApprovedClaimAmountByPolicyId(policy.getId(), approvedStatuses);
        Double coverageAmount = policy.getPlan().getCoverageAmount();

        Double remainingCoverage = coverageAmount - approvedClaimsTotal;
        if (remainingCoverage < 0) remainingCoverage = 0.0;

        if (claim.getClaimAmount() > remainingCoverage) {
            throw new CoverageExhaustedException(
                    "Requested claim amount exceeds the remaining policy coverage.",
                    coverageAmount,
                    approvedClaimsTotal,
                    remainingCoverage,
                    claim.getClaimAmount()
            );
        }

        ClaimStatus oldStatus = claim.getClaimStatus();
        claim.setAdminRemarks(remarks);
        claim.setClaimStatus(ClaimStatus.APPROVED);
        claim.setUpdatedAt(LocalDateTime.now());

        Claim updatedClaim = claimRepository.save(claim);
        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(updatedClaim.getId());

        historyService.recordStatusChange(claim, oldStatus, ClaimStatus.APPROVED, remarks, admin);

        // Send email notification to the customer
        notifyCustomer(updatedClaim, "APPROVED", remarks);

        log.info("Claim {} approved by admin {}", updatedClaim.getClaimNumber(), adminEmail);
        return toEnhancedClaimResponse(updatedClaim, byClaimId);
    }

    @Override
    public ClaimResponseDto rejectClaim(
            Long claimId,
            String remarks,
            String adminEmail) {

        log.info("Admin {} rejecting claimId={}", adminEmail, claimId);

        Claim claim = getClaimEntity(claimId);
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        // Admin can reject from either RECOMMENDED_APPROVAL or RECOMMENDED_REJECTION (override)
        if (claim.getClaimStatus() != ClaimStatus.RECOMMENDED_APPROVAL
                && claim.getClaimStatus() != ClaimStatus.RECOMMENDED_REJECTION) {
            log.warn("Claim {} cannot be rejected. Current status={}", claim.getClaimNumber(), claim.getClaimStatus());
            throw new BusinessException("Claim must be reviewed by an agent before admin can reject. Current status: " + claim.getClaimStatus());
        }

        ClaimStatus oldStatus = claim.getClaimStatus();
        claim.setAdminRemarks(remarks);
        claim.setClaimStatus(ClaimStatus.REJECTED);
        claim.setUpdatedAt(LocalDateTime.now());

        Claim updatedClaim = claimRepository.save(claim);
        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(updatedClaim.getId());

        historyService.recordStatusChange(claim, oldStatus, ClaimStatus.REJECTED, remarks, admin);

        // Send email notification to the customer
        notifyCustomer(updatedClaim, "REJECTED", remarks);

        log.info("Claim {} rejected by admin {}", updatedClaim.getClaimNumber(), adminEmail);
        return toEnhancedClaimResponse(updatedClaim, byClaimId);
    }

    @Override
    public ClaimResponseDto getClaimById(Long claimId, String email, String role) {
        log.debug("Fetching claim by id={}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        if ("CUSTOMER".equals(role) && !claim.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("Access denied. You can only view your own claims.");
        }

        return toEnhancedClaimResponse(claim, claimDocumentRepository.findByClaimId(claimId));
    }

    @Override
    public ClaimResponseDto getClaimByNumber(String claimNumber, String email, String role) {
        log.debug("Fetching claim by number={}", claimNumber);
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with number: " + claimNumber));

        if ("CUSTOMER".equals(role) && !claim.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("Access denied. You can only view your own claims.");
        }

        return toEnhancedClaimResponse(claim, claimDocumentRepository.findByClaimId(claim.getId()));
    }

    @Override
    public List<ClaimResponseDto> getMyClaims(String customerEmail) {
        log.debug("Fetching claims for customer={}", customerEmail);
        List<Claim> claims = claimRepository.findByPolicyCustomerUserEmail(customerEmail);

        return claims.stream()
                .map(claim -> {
                    List<ClaimDocument> docs = claimDocumentRepository.findByClaimId(claim.getId());
                    return toClaimResponse(claim, docs); // Use standard to keep lists light
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<ClaimResponseDto> getAllClaims(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String status) {

        log.debug("Fetching all claims page={}, size={}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Claim> claims;

        if (status != null && !status.isEmpty()) {
            claims = claimRepository.findByClaimStatus(ClaimStatus.valueOf(status), pageable);
        } else {
            claims = claimRepository.findAll(pageable);
        }

        // Use standard response for list views to prevent N+1 payload bloat
        return claims.map(claim -> toClaimResponse(claim, claimDocumentRepository.findByClaimId(claim.getId())));
    }

    @Override
    public Page<ClaimResponseDto> getInsuranceOperationsOfficerAssignedClaims(
            String agentEmail,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.debug("Fetching assigned claims for agent={}", agentEmail);

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Claim> claims = claimRepository.findByAssignedInsuranceOperationsOfficerEmail(agentEmail, pageable);

        // Use enhanced response so agents can see documents and plan details for review
        return claims.map(claim -> toEnhancedClaimResponse(claim, claimDocumentRepository.findByClaimId(claim.getId())));
    }

    private Claim getClaimEntity(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));
    }

    private void validateFinalStatus(Claim claim) {
        if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
            log.warn("Modification attempted on finalized claim {}", claim.getClaimNumber());
            throw new BusinessException("Approved or rejected claim cannot be modified");
        }
    }

    private String generateClaimNumber() {
        return "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void recordHistory(Claim claim, ClaimStatus previous, ClaimStatus next, String remarks, User by) {
        historyService.recordStatusChange(claim, previous, next, remarks, by);
    }

    private void notifyCustomer(Claim claim, String status, String remarks) {
        try {
            Policy policy = claim.getPolicy();
            if (policy != null && policy.getCustomer() != null && policy.getCustomer().getUser() != null) {
                User customerUser = policy.getCustomer().getUser();
                emailService.sendClaimStatusNotification(
                        customerUser.getEmail(),
                        customerUser.getFullName(),
                        claim.getClaimNumber(),
                        status,
                        remarks
                );
            }
        } catch (Exception e) {
            log.error("Failed to send claim notification for claim {}: {}", claim.getClaimNumber(), e.getMessage());
        }
    }

    private String buildAssignmentMessage(Claim claim) {
        if (claim.getAssignedInsuranceOperationsOfficer() == null) {
            return null;
        }

        String customerName = "Unknown";
        if (claim.getPolicy() != null && claim.getPolicy().getCustomer() != null
                && claim.getPolicy().getCustomer().getUser() != null) {
            customerName = claim.getPolicy().getCustomer().getUser().getFullName();
        }

        ClaimStatus status = claim.getClaimStatus();
        if (status == ClaimStatus.ASSIGNED) {
            return "This claim has been assigned to you for review by the Admin. "
                    + "You have been assigned to review the claim submitted by Customer: " + customerName + ".";
        } else if (status == ClaimStatus.RECOMMENDED_APPROVAL) {
            return "You have recommended this claim for approval. Awaiting Admin's final decision.";
        } else if (status == ClaimStatus.RECOMMENDED_REJECTION) {
            return "You have recommended this claim for rejection. Awaiting Admin's final decision.";
        } else if (status == ClaimStatus.APPROVED) {
            return "This claim has been approved by the Admin.";
        } else if (status == ClaimStatus.REJECTED) {
            return "This claim has been rejected by the Admin.";
        }
        return null;
    }

    private ClaimResponseDto toClaimResponse(Claim cl, List<ClaimDocument> docs) {
        Policy po = cl.getPolicy();
        Customer c = po != null ? po.getCustomer() : null;
        User assignedAgent = cl.getAssignedInsuranceOperationsOfficer();
        return ClaimResponseDto.builder()
                .claimId(cl.getId())
                .claimNumber(cl.getClaimNumber())
                .policyId(po != null ? po.getId() : null)
                .policyNumber(po != null ? po.getPolicyNumber() : null)
                .incidentDate(cl.getIncidentDate() != null ? cl.getIncidentDate().toString() : null)
                .customerName(c != null && c.getUser() != null ? c.getUser().getFullName() : null)
                .claimAmount(cl.getClaimAmount())
                .claimReason(cl.getClaimReason())
                .claimStatus(cl.getClaimStatus() != null ? cl.getClaimStatus().name() : null)
                .insuranceOperationsOfficerRemarks(cl.getAgentRemarks())
                .adminRemarks(cl.getAdminRemarks())
                .assignedInsuranceOperationsOfficerId(assignedAgent != null ? assignedAgent.getId() : null)
                .assignedInsuranceOperationsOfficerName(assignedAgent != null ? assignedAgent.getFullName() : null)
                .assignedAt(cl.getAssignedAt() != null ? cl.getAssignedAt() : null)
                .assignmentMessage(buildAssignmentMessage(cl))
                .documents(docs == null
                        ? List.of()
                        : docs.stream()
                        .map(doc -> ClaimDocumentResponse.builder()
                                .claimDocumentId(doc.getId())
                                .documentName(doc.getDocumentName())
                                .documentType(doc.getDocumentType())
                                .documentReference(doc.getDocumentReference())
                                .uploadedDate(doc.getUploadedDate())
                                .build())
                        .toList())
                .build();
    }

    private ClaimResponseDto toEnhancedClaimResponse(Claim cl, List<ClaimDocument> docs) {
        ClaimResponseDto response = toClaimResponse(cl, docs);

        Policy po = cl.getPolicy();
        if (po != null) {
            PolicyPlan plan = po.getPlan();
            if (plan != null) {
                response.setPlanDetails(ClaimResponseDto.PlanDetailsDto.builder()
                        .planName(plan.getPlanName())
                        .coverageAmount(plan.getCoverageAmount())
                        .premiumAmount(plan.getPremiumAmount())
                        .premiumType(plan.getPremiumType() != null ? plan.getPremiumType().name() : null)
                        .duration(plan.getDuration())
                        .termsAndConditions(plan.getTermsAndConditions())
                        .build());
            }

            Customer customer = po.getCustomer();
            if (customer != null && customer.getUser() != null) {
                List<Claim> allCustomerClaims = claimRepository.findByPolicyCustomerUserEmail(customer.getUser().getEmail());

                List<ClaimResponseDto.ClaimHistoryDto> history = new ArrayList<>();
                int policyClaimsCount = 0;
                double approvedPolicyClaimAmount = 0.0;

                for (Claim histClaim : allCustomerClaims) {
                    Policy histPolicy = histClaim.getPolicy();

                    // 1. Calculate Summary ONLY for this specific Policy
                    if (histPolicy != null && histPolicy.getId().equals(po.getId())) {
                        policyClaimsCount++;
                        // Only APPROVED claims reduce the available coverage
                        if (histClaim.getClaimStatus() == ClaimStatus.APPROVED) {
                            approvedPolicyClaimAmount += (histClaim.getClaimAmount() != null ? histClaim.getClaimAmount() : 0.0);
                        }
                    }

                    // 2. Build History (Skip the current claim so it doesn't show in its own "Previous History" list)
                    if (histClaim.getId().equals(cl.getId())) continue;

                    PolicyPlan histPlan = histPolicy != null ? histPolicy.getPlan() : null;
                    String histPlanName = histPlan != null ? histPlan.getPlanName() : "N/A";
                    Double histCoverage = histPlan != null ? histPlan.getCoverageAmount() : 0.0;

                    history.add(ClaimResponseDto.ClaimHistoryDto.builder()
                            .claimNumber(histClaim.getClaimNumber())
                            .planName(histPlanName)
                            .coverageAmount(histCoverage)
                            .claimedAmount(histClaim.getClaimAmount())
                            .claimStatus(histClaim.getClaimStatus() != null ? histClaim.getClaimStatus().name() : null)
                            .claimDate(histClaim.getCreatedAt() != null ? histClaim.getCreatedAt().toString() : null)
                            .build());
                }

                // Sort history by date descending
                history.sort((a, b) -> {
                    if (a.getClaimDate() == null && b.getClaimDate() == null) return 0;
                    if (a.getClaimDate() == null) return 1;
                    if (b.getClaimDate() == null) return -1;
                    return b.getClaimDate().compareTo(a.getClaimDate());
                });

                response.setCustomerClaimHistory(history);

                // 3. Finalize Plan Summary calculations
                if (plan != null) {
                    double remaining = plan.getCoverageAmount() - approvedPolicyClaimAmount;
                    response.setPlanSummary(ClaimResponseDto.PlanSummaryDto.builder()
                            .totalPreviousClaims(policyClaimsCount)
                            .totalPreviousClaimAmount(approvedPolicyClaimAmount)
                            .remainingCoverage(Math.max(remaining, 0.0)) // Ensures it never goes below zero
                            .build());
                }
            }
        }
        return response;
    }
}