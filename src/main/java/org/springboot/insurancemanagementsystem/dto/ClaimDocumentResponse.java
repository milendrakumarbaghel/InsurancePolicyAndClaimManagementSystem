package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocumentResponse {
    private Long id;
    private String documentName;
    private String documentType;
    private String documentReference;
    private LocalDateTime uploadedDate;
}
