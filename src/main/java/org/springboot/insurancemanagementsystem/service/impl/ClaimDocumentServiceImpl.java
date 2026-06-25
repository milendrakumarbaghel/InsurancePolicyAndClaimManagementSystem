package org.springboot.insurancemanagementsystem.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaimDocumentServiceImpl
        implements ClaimDocumentService {

    private final ClaimRepository claimRepository;

    private final ClaimDocumentRepository claimDocumentRepository;

//    private final CloudinaryService cloudinaryService;
    private final Cloudinary cloudinary;

    @Override
    public ClaimDocumentResponse uploadDocument(Long claimId, MultipartFile file) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found"));

        String documentUrl = uploadFile(file); // use your method

        ClaimDocument document = ClaimDocument.builder()
                .claim(claim)
                .documentName(file.getOriginalFilename())
                .documentType(file.getContentType())
                .documentReference(documentUrl)
                .uploadedDate(LocalDateTime.now())
                .build();

        ClaimDocument saved = claimDocumentRepository.save(document);

        return mapToResponse(saved);
    }


    public String uploadFile(MultipartFile file) {
        try {
            String resourceType = "auto";

            // Optional: force PDFs to raw
            if ("application/pdf".equals(file.getContentType())) {
                resourceType = "raw";
            }

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

//    @Override
//    public ClaimDocumentResponse uploadDocument(
//            Long claimId,
//            MultipartFile file) {
//
//        Claim claim = claimRepository.findById(claimId)
//                .orElseThrow(() ->
//                        new BusinessException(
//                                "Claim not found"));
//
//        String documentUrl =
//                cloudinaryService.uploadFile(file);
//
//        ClaimDocument document =
//                ClaimDocument.builder()
//                        .claim(claim)
//                        .documentName(file.getOriginalFilename())
//                        .documentType(file.getContentType())
//                        .documentReference(documentUrl)
//                        .uploadedDate(LocalDateTime.now())
//                        .build();
//
//        ClaimDocument saved =
//                claimDocumentRepository.save(document);
//
//        return mapToResponse(saved);
//    }



//    @Override
//    public List<ClaimDocumentResponse> getDocumentsByClaimId(
//            Long claimId) {
//
//        return claimDocumentRepository
//                .findByClaimId(claimId)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }
    @Override
    public List<ClaimDocumentResponse> getDocumentsByClaimId(Long claimId, String email, String role) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found"));

        if ("CUSTOMER".equals(role) && !claim.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("Access denied. You can only view documents for your own claims.");
        }

        return claimDocumentRepository.findByClaimId(claimId)
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
                .claimDocumentId(document.getId())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .documentReference(document.getDocumentReference())
                .uploadedDate(document.getUploadedDate())
                .build();
    }
}
