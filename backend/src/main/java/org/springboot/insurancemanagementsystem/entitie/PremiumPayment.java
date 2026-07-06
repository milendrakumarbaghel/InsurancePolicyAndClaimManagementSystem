package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.*;
import lombok.*;
import org.springboot.insurancemanagementsystem.enums.PaymentMode;
import org.springboot.insurancemanagementsystem.enums.PaymentStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private Policy policy;

    private Double amount;

    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private PaymentMode paymentMode;

    @Column(unique = true)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private PaymentStatus status;

    private LocalDateTime createdAt;
}