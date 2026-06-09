package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.insurancemanagementsystem.dto.ClaimDocumentResponse;
import org.springboot.insurancemanagementsystem.entitie.Claim;
import org.springboot.insurancemanagementsystem.entitie.ClaimDocument;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.repository.ClaimDocumentRepository;
import org.springboot.insurancemanagementsystem.repository.ClaimRepository;
import org.springboot.insurancemanagementsystem.service.ClaimDocumentService;
import org.springboot.insurancemanagementsystem.service.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimDocumentServiceImpl
        implements ClaimDocumentService {

    private final ClaimRepository claimRepository;

    private final ClaimDocumentRepository claimDocumentRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    public ClaimDocumentResponse uploadDocument(
            Long claimId,
            MultipartFile file) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Claim not found"));

        String documentUrl =
                cloudinaryService.uploadFile(file);

        ClaimDocument document =
                ClaimDocument.builder()
                        .claim(claim)
                        .documentName(file.getOriginalFilename())
                        .documentType(file.getContentType())
                        .documentReference(documentUrl)
                        .uploadedDate(LocalDateTime.now())
                        .build();

        ClaimDocument saved =
                claimDocumentRepository.save(document);

        return mapToResponse(saved);
    }

    @Override
    public List<ClaimDocumentResponse> getDocumentsByClaimId(
            Long claimId) {

        return claimDocumentRepository
                .findByClaimId(claimId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ClaimDocumentResponse getDocumentById(
            Long documentId) {

        ClaimDocument document =
                claimDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Document not found"));

        return mapToResponse(document);
    }

    @Override
    public void deleteDocument(
            Long documentId) {

        ClaimDocument document =
                claimDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Document not found"));

        claimDocumentRepository.delete(document);
    }

    private ClaimDocumentResponse mapToResponse(
            ClaimDocument document) {

        return ClaimDocumentResponse.builder()
                .id(document.getId())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .documentReference(document.getDocumentReference())
                .uploadedDate(document.getUploadedDate())
                .build();
    }
}
