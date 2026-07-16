package org.springboot.insurancemanagementsystem.entitie;

import jakarta.persistence.*;
import lombok.*;
import org.springboot.insurancemanagementsystem.enums.Role;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private String firstName;

    private String middleName;

    private String lastName;

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

    @Transient
    public String getFullName() {
        return Stream.of(firstName, middleName, lastName)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}