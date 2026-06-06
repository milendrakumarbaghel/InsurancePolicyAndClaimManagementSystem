package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class InsuranceProductController {

    private final InsuranceProductService insuranceProductService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto requestDto) {

        ProductResponseDto response =
                insuranceProductService.createProduct(requestDto);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequestDto requestDto) {

        return ResponseEntity.ok(
                insuranceProductService.updateProduct(
                        productId,
                        requestDto));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<ProductResponseDto> getProductById(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                insuranceProductService.getProductById(
                        productId));
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

        return ResponseEntity.ok(
                insuranceProductService.getAllProducts(
                        page,
                        size,
                        sortBy,
                        sortDir));
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

        return ResponseEntity.ok(
                insuranceProductService.getActiveProducts(
                        page,
                        size,
                        sortBy,
                        sortDir));
    }

    @PatchMapping("/{productId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateProduct(
            @PathVariable Long productId) {

        insuranceProductService.deactivateProduct(productId);

        return ResponseEntity.ok(
                "Product deactivated successfully");
    }
}