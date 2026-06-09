package org.springboot.insurancemanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import org.springboot.insurancemanagementsystem.dto.ClaimDocumentResponse;
import org.springboot.insurancemanagementsystem.service.ClaimDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/claim-documents")
@RequiredArgsConstructor
public class ClaimDocumentController {

    private final ClaimDocumentService claimDocumentService;

    @PostMapping("/upload/{claimId}")
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
    public ResponseEntity<List<ClaimDocumentResponse>>
    getDocumentsByClaimId(
            @PathVariable Long claimId) {

        return ResponseEntity.ok(
                claimDocumentService
                        .getDocumentsByClaimId(
                                claimId
                        )
        );
    }

    @GetMapping("/{documentId}")
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
