package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.ProductRequestDto;
import org.springboot.insurancemanagementsystem.dto.ProductResponseDto;
import org.springframework.data.domain.Page;

public interface InsuranceProductService {

    ProductResponseDto createProduct(ProductRequestDto requestDto);

    ProductResponseDto updateProduct(Long productId,
                                     ProductRequestDto requestDto);

    ProductResponseDto getProductById(Long productId);

    Page<ProductResponseDto> getAllProducts(int page,
                                            int size,
                                            String sortBy,
                                            String sortDir);

    Page<ProductResponseDto> getActiveProducts(int page,
                                               int size,
                                               String sortBy,
                                               String sortDir);

    void deactivateProduct(Long productId);
    void activateProduct(Long productId);
}
