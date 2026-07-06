package org.springboot.insurancemanagementsystem.service;


import org.springboot.insurancemanagementsystem.dto.ClaimDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClaimDocumentService {

    ClaimDocumentResponse uploadDocument(
            Long claimId,
            MultipartFile file
    );

    List<ClaimDocumentResponse> getDocumentsByClaimId(Long claimId, String email, String role);

    ClaimDocumentResponse getDocumentById(
            Long documentId
    );

    void deleteDocument(
            Long documentId
    );
}
