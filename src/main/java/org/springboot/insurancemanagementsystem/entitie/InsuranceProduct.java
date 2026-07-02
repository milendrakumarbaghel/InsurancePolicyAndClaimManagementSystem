package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.*;
import lombok.*;
import org.springboot.insurancemanagementsystem.enums.ProductType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ProductType productType;

    private String description;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}