package org.springboot.insurancemanagementsystem.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.*;
import org.springboot.insurancemanagementsystem.entitie.*;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.*;
import org.springboot.insurancemanagementsystem.service.ClaimService;
import org.springboot.insurancemanagementsystem.service.ClaimStatusHistoryService;
import org.springboot.insurancemanagementsystem.service.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private final CloudinaryService cloudinaryService;
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ClaimStatusHistoryService historyService;

    @Override
    @Transactional
    public ClaimResponseDto raiseClaim(
            ClaimRequestDto request,
            MultipartFile document,
            String customerEmail) {

        log.info("Customer {} attempting to raise claim for policyId={}",
                customerEmail,
                request.getPolicyId());

        Customer customer =
                customerRepository.findByUserEmail(customerEmail)
                        .orElseThrow(() -> {
                            log.warn("Customer not found for email={}",
                                    customerEmail);
                            return new ResourceNotFoundException(
                                    "Customer not found");
                        });

        Policy policy =
                policyRepository.findById(request.getPolicyId())
                        .orElseThrow(() -> {
                            log.warn("Policy not found. policyId={}",
                                    request.getPolicyId());
                            return new ResourceNotFoundException(
                                    "Policy not found");
                        });

        if (!policy.getCustomer()
                .getId()
                .equals(customer.getId())) {

            log.warn(
                    "Claim rejected. Policy {} does not belong to customer {}",
                    policy.getId(),
                    customerEmail);

            throw new BusinessException(
                    "Policy does not belong to customer");
        }

        if (policy.getStatus() != PolicyStatus.ACTIVE) {

            log.warn(
                    "Claim rejected. Policy {} is not ACTIVE",
                    policy.getPolicyNumber());

            throw new BusinessException(
                    "Claim can be raised only on active policy");
        }

        if (request.getClaimAmount()
                .compareTo(
                        policy.getPlan()
                                .getCoverageAmount()) > 0) {

            log.warn(
                    "Claim amount {} exceeds coverage amount {}",
                    request.getClaimAmount(),
                    policy.getPlan().getCoverageAmount());

            throw new BusinessException(
                    "Claim amount exceeds coverage");
        }

        validateClaimDocument(document);

        String documentUrl =
                cloudinaryService.uploadFile(document);

        log.info("Claim document uploaded successfully. URL={}",
                documentUrl);

        Claim claim = new Claim();

        claim.setClaimNumber(generateClaimNumber());
        claim.setPolicy(policy);
        claim.setClaimAmount(request.getClaimAmount());
        claim.setClaimReason(request.getClaimReason());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setDocumentUrl(documentUrl);
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        claim.setCreatedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());

        Claim savedClaim =
                claimRepository.save(claim);

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
                                        .documentName(
                                                d.getDocumentName())
                                        .documentType(
                                                d.getDocumentType())
                                        .documentReference(
                                                d.getDocumentReference())
                                        .uploadedDate(
                                                LocalDateTime.now())
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
                customer.getUser());

        return toClaimResponse(savedClaim, savedDocs);
    }

    private void validateClaimDocument(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {

            log.warn("Claim document is missing");

            throw new BusinessException(
                    "Claim document is required");
        }

        long maxSize = 5 * 1024 * 1024; // 5 MB

        if (file.getSize() > maxSize) {

            log.warn("Document size exceeded. Size={} bytes",
                    file.getSize());

            throw new BusinessException(
                    "Maximum file size allowed is 5 MB");
        }

        String contentType =
                file.getContentType();

        if (contentType == null) {

            throw new BusinessException(
                    "Invalid file type");
        }

        List<String> allowedTypes =
                List.of(
                        "application/pdf",
                        "image/jpeg",
                        "image/jpg",
                        "image/png"
                );

        if (!allowedTypes.contains(contentType)) {

            log.warn("Unsupported document type={}",
                    contentType);

            throw new BusinessException(
                    "Only PDF, JPG, JPEG and PNG files are allowed");
        }

        log.info("Document validation successful. Type={}, Size={} bytes",
                contentType,
                file.getSize());
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

        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(updatedClaim.getId());

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

        return toClaimResponse(updatedClaim, byClaimId);
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

        claim.setAdminRemarks(remarks);

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

        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(updatedClaim.getId());

        historyService.recordStatusChange(
                claim,
                oldStatus,
                ClaimStatus.APPROVED,
                remarks,
                admin);

        log.info("Claim {} approved by admin {}",
                updatedClaim.getClaimNumber(),
                adminEmail);
        return toClaimResponse(updatedClaim, byClaimId);
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
        claim.setAdminRemarks(remarks);

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
        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(updatedClaim.getId());
        return toClaimResponse(updatedClaim, byClaimId);
    }

    @Override
    public ClaimResponseDto getClaimById(
            Long claimId) {

        log.debug("Fetching claim by id={}", claimId);
        Claim claimEntity = getClaimEntity(claimId);
        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(claimEntity.getId());
        return toClaimResponse(claimEntity, byClaimId);
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

        List<ClaimDocument> byClaimId = claimDocumentRepository.findByClaimId(claim.getId());
        return toClaimResponse(claim, byClaimId);
    }

    @Override
    public List<ClaimResponseDto> getMyClaims(String customerEmail) {

        log.debug("Fetching claims for customer={}",
                customerEmail);

        List<Claim> claims =
                claimRepository.findByPolicyCustomerUserEmail(customerEmail);

        return claims.stream()
                .map(claim -> {
                    List<ClaimDocument> docs =
                            claimDocumentRepository.findByClaimId(claim.getId());

                    return toClaimResponse(claim, docs);
                })
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
                .map(claim -> toClaimResponse(
                        claim,
                        claimDocumentRepository.findByClaimId(claim.getId())));
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

//    private ClaimResponseDto mapToResponseDto(Claim claim) {
//
//        ClaimResponseDto dto =
//                modelMapper.map(
//                        claim,
//                        ClaimResponseDto.class);
//
//        if (claim.getPolicy() != null) {
//
//            dto.setPolicyId(
//                    claim.getPolicy().getId());
//
//            dto.setPolicyNumber(
//                    claim.getPolicy().getPolicyNumber());
//
//            if (claim.getPolicy().getCustomer() != null
//                    && claim.getPolicy().getCustomer().getUser() != null) {
//
//                dto.setCustomerName(
//                        claim.getPolicy()
//                                .getCustomer()
//                                .getUser()
//                                .getFullName());
//            }
//        }
//
//        if (claim.getClaimStatus() != null) {
//            dto.setClaimStatus(
//                    claim.getClaimStatus().name());
//        }
//
//        dto.setAgentRemarks(
//                claim.getAgentRemarks());
//
//        dto.setAdminRemarks(
//                claim.getAdminRemarks());
//
//        return dto;
//    }

    private void recordHistory(Claim claim, ClaimStatus previous, ClaimStatus next,
                               String remarks, User by) {
        historyService.recordStatusChange(claim, previous, next, remarks, by);
    }

    private ClaimResponseDto toClaimResponse(Claim cl, List<ClaimDocument> docs) {
        Policy po = cl.getPolicy();
        Customer c = po != null ? po.getCustomer() : null;
        return ClaimResponseDto.builder()
                .claimId(cl.getId())
                .claimNumber(cl.getClaimNumber())
                .policyId(po != null ? po.getId() : null)
                .policyNumber(po != null ? po.getPolicyNumber() : null)
                .customerName(c != null && c.getUser() != null ? c.getUser().getFullName() : null)
                .claimAmount(cl.getClaimAmount() == null ? null : cl.getClaimAmount())
                .claimStatus(cl.getClaimStatus() == null ? null : cl.getClaimStatus().name())
                .agentRemarks(cl.getAgentRemarks())
                .adminRemarks(cl.getAdminRemarks())
                .documents(docs == null
                        ? List.of()
                        : docs.stream()
                        .map(doc -> ClaimDocumentResponse.builder()
                                .claimDocumentId(doc.getId())
                                .documentName(doc.getDocumentName())
                                .documentType(doc.getDocumentType())
//                                .documentUrl()
                                .documentReference(doc.getDocumentReference())
                                .uploadedDate(doc.getUploadedDate())
                                .build())
                        .toList())
                .build();
    }
}