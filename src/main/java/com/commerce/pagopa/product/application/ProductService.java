package com.commerce.pagopa.product.application;

import com.commerce.pagopa.global.exception.ProductNotFoundException;
import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllWithActiveAndSoldOut(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(ProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto find(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return ProductResponseDto.from(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllByCategory(Long categoryId, Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByCategoryIdAndStatusIn(
                categoryId, List.of(ProductStatus.ACTIVE, ProductStatus.SOLDOUT), pageable
        );
        return productPage.map(ProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> search(@NonNull ProductSearchCondition condition) {
        return productRepository.searchProducts(condition).stream()
                .map(ProductResponseDto::from)
                .toList();
    }
}
