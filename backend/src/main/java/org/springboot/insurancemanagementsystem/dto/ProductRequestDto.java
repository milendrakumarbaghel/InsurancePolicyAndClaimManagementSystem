package org.springboot.insurancemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springboot.insurancemanagementsystem.enums.ProductType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s'(),.-]+$",
            message = "Product name contains invalid special characters"
    )
    private String productName;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Description cannot contain HTML tags or script character sequences (< or >)"
    )
    private String description;

    @NotNull(message = "Active status is required")
    private Boolean active;
}