package org.springboot.insurancemanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.ClaimDocumentResponse;
import org.springboot.insurancemanagementsystem.security.util.SecurityUtil;
import org.springboot.insurancemanagementsystem.service.ClaimDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/claim-documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class ClaimDocumentController {

    private final ClaimDocumentService claimDocumentService;

    @PostMapping("/upload/{claimId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ClaimDocumentResponse>
    uploadDocument(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file) {

        ClaimDocumentResponse response =
                claimDocumentService.uploadDocument(
                        claimId,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/claim/{claimId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<List<ClaimDocumentResponse>>
    getDocumentsByClaimId(
            @PathVariable Long claimId, Authentication authentication) {

        log.info("REST request to get Documents for Claim ID : {}", claimId);

        String email = authentication.getName();
        String role = SecurityUtil.extractRoleFromAuthentication(authentication);

        List<ClaimDocumentResponse> responses = claimDocumentService.getDocumentsByClaimId(claimId, email, role);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")  //
    public ResponseEntity<ClaimDocumentResponse>
    getDocumentById(
            @PathVariable Long documentId) {

        return ResponseEntity.ok(
                claimDocumentService
                        .getDocumentById(
                                documentId
                        )
        );
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String>
    deleteDocument(
            @PathVariable Long documentId) {

        claimDocumentService
                .deleteDocument(documentId);

        return ResponseEntity.ok(
                "Document deleted successfully"
        );
    }
}
