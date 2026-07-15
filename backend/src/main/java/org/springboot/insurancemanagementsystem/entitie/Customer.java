package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Nominee> nominees = new java.util.ArrayList<>();

    private LocalDate dateOfBirth;

    private String address;
    private String city;
    private String state;
    private String pinCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}