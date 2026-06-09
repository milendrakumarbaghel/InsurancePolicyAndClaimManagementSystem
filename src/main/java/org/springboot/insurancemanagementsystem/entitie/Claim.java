package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.*;
import lombok.*;
import org.springboot.insurancemanagementsystem.enums.ClaimStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String claimNumber;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private Policy policy;

    private Double claimAmount;

    private String claimReason;

    private LocalDate incidentDate;

    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;

    private String agentRemarks;
    private String adminRemarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}