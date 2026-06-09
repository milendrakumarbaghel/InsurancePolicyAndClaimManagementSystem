package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.ProductRequestDto;
import org.springboot.insurancemanagementsystem.dto.ProductResponseDto;
import org.springboot.insurancemanagementsystem.service.InsuranceProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class InsuranceProductController {

    private final InsuranceProductService insuranceProductService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto requestDto) {

        log.info("Product creation request received for product: {}",
                requestDto.getProductName());

        ProductResponseDto response =
                insuranceProductService.createProduct(requestDto);

        log.info("Product created successfully. Product ID: {}, Product Name: {}",
                response.getProductId(),
                response.getProductName());

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequestDto requestDto) {

        log.info("Product update request received. Product ID: {}",
                productId);

        ProductResponseDto response =
                insuranceProductService.updateProduct(
                        productId,
                        requestDto);

        log.info("Product updated successfully. Product ID: {}",
                productId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<ProductResponseDto> getProductById(
            @PathVariable Long productId) {

        log.info("Fetching product details. Product ID: {}",
                productId);

        ProductResponseDto response =
                insuranceProductService.getProductById(
                        productId);

        log.info("Product details retrieved successfully. Product ID: {}",
                productId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        log.info(
                "Fetching all products | page: {}, size: {}, sortBy: {}, sortDir: {}",
                page,
                size,
                sortBy,
                sortDir);

        Page<ProductResponseDto> products =
                insuranceProductService.getAllProducts(
                        page,
                        size,
                        sortBy,
                        sortDir);

        log.info("Retrieved {} product records",
                products.getNumberOfElements());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<Page<ProductResponseDto>> getActiveProducts(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        log.info(
                "Fetching active products | page: {}, size: {}, sortBy: {}, sortDir: {}",
                page,
                size,
                sortBy,
                sortDir);

        Page<ProductResponseDto> products =
                insuranceProductService.getActiveProducts(
                        page,
                        size,
                        sortBy,
                        sortDir);

        log.info("Retrieved {} active product records ", products.getNumberOfElements());

        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{productId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateProduct(
            @PathVariable Long productId) {

        log.info("Product deactivation request received. Product ID: {}",
                productId);

        insuranceProductService.deactivateProduct(productId);

        log.info("Product deactivated successfully. Product ID: {}",
                productId);

        return ResponseEntity.ok(
                "Product deactivated successfully");
    }

    @PatchMapping("/{productId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateProduct(
            @PathVariable Long productId) {

        log.info("Product activation request received. Product ID: {}",
                productId);

        insuranceProductService.activateProduct(productId);

        log.info("Product activated successfully. Product ID: {}",
                productId);

        return ResponseEntity.ok(
                "Product activated successfully");
    }
}