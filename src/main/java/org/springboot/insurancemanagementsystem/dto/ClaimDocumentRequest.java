package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentRequest {

    @NotBlank(message = "Document name must not be blank") 
    @Size(max = 150, message = "Document name cannot exceed 150 characters")
    private String documentName;

    @NotBlank(message = "Document type must not be blank") 
    @Size(max = 80, message = "Document type cannot exceed 80 characters")
    private String documentType;

    @NotBlank(message = "Document reference must not be blank") 
    @Size(max = 500, message = "Document reference cannot exceed 500 characters")
    private String documentReference;
}