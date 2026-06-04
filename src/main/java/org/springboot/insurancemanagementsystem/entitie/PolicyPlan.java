package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PolicyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private InsuranceProduct product;

    private String planName;

    private Double coverageAmount;
    private Double premiumAmount;

    @Enumerated(EnumType.STRING)
    private PremiumType premiumType;

    private Integer duration;

    @Column(length = 2000)
    private String termsAndConditions;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}