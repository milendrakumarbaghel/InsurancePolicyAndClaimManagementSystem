package org.springboot.insurancemanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.ClaimStatusHistoryResponse;
import org.springboot.insurancemanagementsystem.service.ClaimStatusHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claim-history")
@RequiredArgsConstructor
@Slf4j
public class ClaimStatusHistoryController {

    private final ClaimStatusHistoryService claimStatusHistoryService;

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<Page<ClaimStatusHistoryResponse>> getClaimHistory(

            @PathVariable Long claimId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        log.info(
                "Fetching claim history for claimId: {} | page: {}, size: {}, sortBy: {}, sortDir: {}",
                claimId,
                page,
                size,
                sortBy,
                sortDir
        );

        Page<ClaimStatusHistoryResponse> history =
                claimStatusHistoryService.getClaimHistory(
                        claimId,
                        page,
                        size,
                        sortBy,
                        sortDir
                );

        log.info(
                "Retrieved {} claim history records for claimId: {}",
                history.getNumberOfElements(),
                claimId
        );

        return ResponseEntity.ok(history);
    }
}