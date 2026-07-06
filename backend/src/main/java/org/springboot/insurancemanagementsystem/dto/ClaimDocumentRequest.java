package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentRequest {

    @NotBlank(message = "Document name must not be blank")
    @Size(min = 3, max = 150, message = "Document name must be between 3 and 150 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s_()\\.-]+$",
            message = "Document name contains invalid special characters. Use only letters, numbers, spaces, underscores, hyphens, or periods"
    )
    private String documentName;

    @NotBlank(message = "Document type must not be blank")
    @Size(min = 2, max = 80, message = "Document type must be between 2 and 80 characters")
    @Pattern(
            regexp = "^[a-zA-Z\\s/\\-]+$",
            message = "Document type must contain only alphabetic letters, spaces, hyphens, or forward slashes (e.g., Medical Bill, image/jpeg)"
    )
    private String documentType;

    @NotBlank(message = "Document reference must not be blank")
    @Size(min = 5, max = 500, message = "Document reference must be between 5 and 500 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Document reference cannot contain HTML tags or script character sequences (< or >)"
    )
    private String documentReference;
}