package org.springboot.insurancemanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocumentResponseDto {
    private Long id;
    private String documentName;
    private String documentType;
    private String documentReference;
}
