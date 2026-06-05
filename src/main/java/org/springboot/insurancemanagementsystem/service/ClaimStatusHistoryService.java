package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.ClaimStatusHistoryResponse;
import org.springboot.insurancemanagementsystem.entitie.Claim;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;
import org.springframework.data.domain.Page;

public interface ClaimStatusHistoryService {

    void recordStatusChange(
            Claim claim,
            ClaimStatus oldStatus,
            ClaimStatus newStatus,
            String remarks,
            User updatedBy);

    Page<ClaimStatusHistoryResponse> getClaimHistory(
            Long claimId,
            int page,
            int size,
            String sortBy,
            String sortDir);
}
