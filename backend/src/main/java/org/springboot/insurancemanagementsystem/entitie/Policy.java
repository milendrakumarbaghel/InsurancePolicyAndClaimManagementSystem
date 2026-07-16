package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.*;
import lombok.*;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String policyNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private PolicyPlan plan;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PolicyStatus status;

    private Double totalPremiumPaid;

    /** Customer-chosen coverage amount (within plan's min–max range). */
    private Double selectedCoverageAmount;

    /** Customer-chosen duration in months (within plan's min–max range). */
    private Integer selectedDuration;

    /** Customer-chosen premium payment cycle. */
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private org.springboot.insurancemanagementsystem.enums.PremiumType selectedPremiumType;

    /** Per-period premium calculated at purchase time using PremiumCalculator. */
    private Double calculatedPremiumAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}