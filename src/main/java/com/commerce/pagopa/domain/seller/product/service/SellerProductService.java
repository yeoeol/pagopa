package com.commerce.pagopa.domain.seller.product.service;

import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.seller.product.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAll(Long userId, Pageable pageable) {
        Page<Product> pageProduct = productRepository.findAllBySellerId(userId, pageable);
        return pageProduct.map(ProductResponseDto::from);
    }
}
