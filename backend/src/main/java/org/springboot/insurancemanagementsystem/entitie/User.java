package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.*;
import lombok.*;
import org.springboot.insurancemanagementsystem.enums.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Role role;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean emailVerified;
    private boolean mobileVerified;
}