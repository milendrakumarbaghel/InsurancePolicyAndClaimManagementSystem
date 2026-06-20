package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.ClaimStatusHistoryResponse;
import org.springboot.insurancemanagementsystem.entitie.Claim;
import org.springboot.insurancemanagementsystem.entitie.ClaimStatusHistory;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.ClaimRepository;
import org.springboot.insurancemanagementsystem.repository.ClaimStatusHistoryRepository;
import org.springboot.insurancemanagementsystem.service.ClaimStatusHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimStatusHistoryServiceImpl
        implements ClaimStatusHistoryService {

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository historyRepository;
    private final ModelMapper modelMapper;

    @Override
    public void recordStatusChange(
            Claim claim,
            ClaimStatus oldStatus,
            ClaimStatus newStatus,
            String remarks,
            User updatedBy) {

        log.info(
                "Recording claim status change for claimNumber={}, oldStatus={}, newStatus={}, updatedBy={}",
                claim.getClaimNumber(),
                oldStatus,
                newStatus,
                updatedBy != null ? updatedBy.getEmail() : "SYSTEM"
        );

        ClaimStatusHistory history =
                new ClaimStatusHistory();

        history.setClaim(claim);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setUpdatedBy(updatedBy);
        history.setUpdatedDate(LocalDateTime.now());

        historyRepository.save(history);

        log.info(
                "Claim status history saved successfully for claimNumber={}",
                claim.getClaimNumber()
        );
    }

//    @Override
//    public Page<ClaimStatusHistoryResponse> getClaimHistory(
//            Long claimId,
//            int page,
//            int size,
//            String sortBy,
//            String sortDir) {
//
//        log.info(
//                "Fetching claim history for claimId={}, page={}, size={}",
//                claimId,
//                page,
//                size
//        );
//
//        Claim claim = claimRepository
//                .findById(claimId)
//                .orElseThrow(() -> {
//
//                    log.warn(
//                            "Claim not found while fetching history. claimId={}",
//                            claimId
//                    );
//
//                    return new ResourceNotFoundException(
//                            "Claim not found");
//                });
//
//        Sort sort =
//                sortDir.equalsIgnoreCase("asc")
//                        ? Sort.by(sortBy).ascending()
//                        : Sort.by(sortBy).descending();
//
//        Pageable pageable =
//                PageRequest.of(page, size, sort);
//
//        Page<ClaimStatusHistoryResponse> historyPage =
//                historyRepository
//                        .findByClaim(claim, pageable)
//                        .map(this::mapToResponseDto);
//
//        log.info(
//                "Claim history fetched successfully for claimNumber={}, records={}",
//                claim.getClaimNumber(),
//                historyPage.getTotalElements()
//        );
//
//        return historyPage;
//    }
@Override
public Page<ClaimStatusHistoryResponse> getClaimHistory(
        Long claimId, int page, int size, String sortBy, String sortDir, String email, String role) {

    log.info("Fetching claim history for claimId={}, page={}, size={}", claimId, page, size);

    Claim claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

    // Guardrail boundary check preventing cross-tenant user identity snooping
    if ("CUSTOMER".equals(role) && !claim.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
        throw new BusinessException("Access denied. You can only view the history log entries for your own claims.");
    }

    Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<ClaimStatusHistory> historyPage = historyRepository.findByClaim(claim, pageable);
    return historyPage.map(this::mapToResponseDto);
}

    private ClaimStatusHistoryResponse mapToResponseDto(
            ClaimStatusHistory history) {

        ClaimStatusHistoryResponse dto =
                new ClaimStatusHistoryResponse();

        dto.setHistoryId(history.getId());

        dto.setClaimNumber(
                history.getClaim().getClaimNumber());

        dto.setPreviousStatus(
                history.getPreviousStatus());

        dto.setNewStatus(
                history.getNewStatus());

        dto.setRemarks(
                history.getRemarks());

        dto.setUpdatedAt(
                history.getUpdatedDate());

        if (history.getUpdatedBy() != null) {

            dto.setUpdatedBy(
                    history.getUpdatedBy().getFullName());

            dto.setUpdatedByRole(
                    history.getUpdatedBy()
                            .getRole()
                            .name());
        }

        return dto;
    }
}