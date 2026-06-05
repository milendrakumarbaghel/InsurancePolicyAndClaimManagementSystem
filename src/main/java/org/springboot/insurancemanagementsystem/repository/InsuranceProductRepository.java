package org.springboot.insurancemanagementsystem.repository;


import org.springboot.insurancemanagementsystem.entitie.InsuranceProduct;
import org.springboot.insurancemanagementsystem.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceProductRepository
        extends JpaRepository<InsuranceProduct, Long> {

    boolean existsByProductName(String productName);

    Page<InsuranceProduct> findByActive(Boolean active,
                                        Pageable pageable);

    Page<InsuranceProduct> findByProductType(
            ProductType productType,
            Pageable pageable
    );

    Page<InsuranceProduct> findByProductTypeAndActive(
            ProductType productType,
            Boolean active,
            Pageable pageable
    );
}