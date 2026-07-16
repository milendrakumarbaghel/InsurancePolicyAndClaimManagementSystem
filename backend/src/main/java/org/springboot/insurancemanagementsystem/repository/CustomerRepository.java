package org.springboot.insurancemanagementsystem.repository;

import org.springboot.insurancemanagementsystem.entitie.Customer;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser(User user);

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    Page<Customer> findByCity(String city,
                              Pageable pageable);

    Page<Customer> findByState(String state,
                               Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.user.middleName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.user.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);

}