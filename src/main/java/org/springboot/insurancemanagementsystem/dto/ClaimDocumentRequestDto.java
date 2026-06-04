package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocumentRequestDto {
    private Long claimId;
    private String documentName;
    private String documentType;
    private String documentReference;
}
