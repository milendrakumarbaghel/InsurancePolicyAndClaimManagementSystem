package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.ProductRequestDto;
import org.springboot.insurancemanagementsystem.dto.ProductResponseDto;
import org.springboot.insurancemanagementsystem.entitie.InsuranceProduct;
import org.springboot.insurancemanagementsystem.enums.ProductType;
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
public class InsuranceProductServiceImpl implements InsuranceProductService {

    private final InsuranceProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {

        if (productRepository.existsByProductName(requestDto.getProductName())) {
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

        return modelMapper.map(savedProduct, ProductResponseDto.class);
    }

    @Override
    public ProductResponseDto updateProduct(Long productId,
                                            ProductRequestDto requestDto) {

        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id : " + productId));

        product.setProductName(requestDto.getProductName());
        product.setProductType(requestDto.getProductType());
        product.setDescription(requestDto.getDescription());
        product.setActive(requestDto.getActive());

        InsuranceProduct updatedProduct =
                productRepository.save(product);

        return modelMapper.map(updatedProduct, ProductResponseDto.class);
    }

    @Override
    public ProductResponseDto getProductById(Long productId) {

        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id : " + productId));

        return modelMapper.map(product, ProductResponseDto.class);
    }

    @Override
    public Page<ProductResponseDto> getAllProducts(int page,
                                                   int size,
                                                   String sortBy,
                                                   String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return productRepository.findAll(pageable)
                .map(insuranceProduct -> modelMapper.map(insuranceProduct, ProductResponseDto.class));
    }

    @Override
    public Page<ProductResponseDto> getActiveProducts(int page,
                                                      int size,
                                                      String sortBy,
                                                      String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return productRepository
                .findByActiveTrue(pageable)
                .map(insuranceProduct -> modelMapper.map(insuranceProduct, ProductResponseDto.class));
    }

    @Override
    public void deactivateProduct(Long productId) {

        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id : " + productId));

        product.setActive(false);

        productRepository.save(product);
    }
}
