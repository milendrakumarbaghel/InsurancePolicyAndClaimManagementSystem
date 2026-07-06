package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.ProductRequestDto;
import org.springboot.insurancemanagementsystem.dto.ProductResponseDto;
import org.springboot.insurancemanagementsystem.entitie.InsuranceProduct;
import org.springboot.insurancemanagementsystem.exception.DuplicateResourceException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.InsuranceProductRepository;
import org.springboot.insurancemanagementsystem.service.InsuranceProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceProductServiceImpl implements InsuranceProductService {

    private final InsuranceProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {

        log.info("Product creation request received. ProductName={}",
                requestDto.getProductName());

        if (productRepository.existsByProductName(requestDto.getProductName())) {

            log.warn("Product already exists. ProductName={}",
                    requestDto.getProductName());

            throw new DuplicateResourceException(
                    "Product already exists with name : "
                            + requestDto.getProductName());
        }

        InsuranceProduct product = InsuranceProduct.builder()
                .productType(requestDto.getProductType())
                .productName(requestDto.getProductName())
                .description(requestDto.getDescription())
                .createdAt(LocalDateTime.now())
                .active(requestDto.getActive())
                .updatedAt(LocalDateTime.now())
                .build();

        InsuranceProduct savedProduct =
                productRepository.save(product);

        log.info(
                "Product created successfully. ProductId={}, ProductName={}",
                savedProduct.getId(),
                savedProduct.getProductName());

        ProductResponseDto map = modelMapper.map(savedProduct, ProductResponseDto.class);
        map.setProductId(savedProduct.getId());
        return map;
    }

    @Override
    public ProductResponseDto updateProduct(
            Long productId,
            ProductRequestDto requestDto) {

        log.info("Product update request received. ProductId={}",
                productId);

        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> {

                    log.warn(
                            "Product not found for update. ProductId={}",
                            productId);

                    return new ResourceNotFoundException(
                            "Product not found with id : " + productId);
                });

        product.setProductName(requestDto.getProductName());
        product.setProductType(requestDto.getProductType());
        product.setDescription(requestDto.getDescription());
        product.setActive(requestDto.getActive());
        product.setUpdatedAt(LocalDateTime.now());

        InsuranceProduct updatedProduct =
                productRepository.save(product);

        log.info(
                "Product updated successfully. ProductId={}, ProductName={}",
                updatedProduct.getId(),
                updatedProduct.getProductName());

        ProductResponseDto map = modelMapper.map(updatedProduct, ProductResponseDto.class);
        map.setProductId(updatedProduct.getId());
        return map;
    }

    @Override
    public ProductResponseDto getProductById(Long productId) {

        log.info("Fetching product details. ProductId={}",
                productId);

        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> {

                    log.warn(
                            "Product not found. ProductId={}",
                            productId);

                    return new ResourceNotFoundException(
                            "Product not found with id : " + productId);
                });

        ProductResponseDto map = modelMapper.map(product, ProductResponseDto.class);
        map.setProductId(product.getId());
        return map;
    }

    @Override
    public Page<ProductResponseDto> getAllProducts(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info(
                "Fetching all products. Page={}, Size={}, SortBy={}, SortDir={}",
                page,
                size,
                sortBy,
                sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<ProductResponseDto> products =
                productRepository.findAll(pageable)
                        .map(this::mapToResponseDto);

        log.info(
                "Products fetched successfully. TotalRecords={}",
                products.getTotalElements());

        return products;
    }

    @Override
    public Page<ProductResponseDto> getActiveProducts(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info(
                "Fetching active products. Page={}, Size={}, SortBy={}, SortDir={}",
                page,
                size,
                sortBy,
                sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<ProductResponseDto> products =
                productRepository.findAll(pageable)
                        .map(this::mapToResponseDto);

        log.info(
                "Active products fetched successfully. TotalRecords={}",
                products.getTotalElements());

        return products;
    }

    private ProductResponseDto mapToResponseDto(
            InsuranceProduct product) {

        ProductResponseDto dto =
                modelMapper.map(product, ProductResponseDto.class);

        dto.setProductId(product.getId());

        return dto;
    }

    @Override
    public void deactivateProduct(Long productId) {

        log.info(
                "Product deactivation request received. ProductId={}",
                productId);

        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> {

                    log.warn(
                            "Product not found for deactivation. ProductId={}",
                            productId);

                    return new ResourceNotFoundException(
                            "Product not found with id : " + productId);
                });

        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info(
                "Product deactivated successfully. ProductId={}, ProductName={}",
                product.getId(),
                product.getProductName());
    }

    @Override
    public void activateProduct(Long productId) {

        log.info(
                "Product activation request received. ProductId={}",
                productId);

        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> {

                    log.warn(
                            "Product not found for activation. ProductId={}",
                            productId);

                    return new ResourceNotFoundException(
                            "Product not found with id : " + productId);
                });

        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info(
                "Product activated successfully. ProductId={}, ProductName={}",
                product.getId(),
                product.getProductName());
    }
}