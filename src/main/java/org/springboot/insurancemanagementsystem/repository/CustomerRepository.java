package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entitie.Customer;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser(User user);

    Optional<Customer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Page<Customer> findByCity(String city,
                              Pageable pageable);

    Page<Customer> findByState(String state,
                               Pageable pageable);

}