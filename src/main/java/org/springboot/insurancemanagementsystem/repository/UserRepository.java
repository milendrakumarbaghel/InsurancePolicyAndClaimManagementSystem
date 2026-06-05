package org.springboot.insurancemanagementsystem.repository;


import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByActive(Boolean active, Pageable pageable);

    Page<User> findByRoleAndActive(Role role,
                                   Boolean active,
                                   Pageable pageable);
}
