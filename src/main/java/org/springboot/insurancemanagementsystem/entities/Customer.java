package org.springboot.insurancemanagementsystem.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private LocalDate dateOfBirth;

    private String address;
    private String city;
    private String state;
    private String pinCode;

    private String nomineeName;
    private String nomineeRelation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}