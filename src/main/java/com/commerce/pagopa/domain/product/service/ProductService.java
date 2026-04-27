package com.commerce.pagopa.domain.product.service;

import com.commerce.pagopa.domain.product.dto.request.ProductSearchCondition;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.entity.enums.ProductStatus;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
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
    public List<ProductResponseDto> findAllByCategory() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAllWithActiveAndSoldOut() {
        return productRepository.findAll()
                .stream()
                .filter(this::isActiveOrSoldOut)
                .map(ProductResponseDto::from)
                .toList();
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
    public List<ProductResponseDto> search(ProductSearchCondition condition) {
        return productRepository.searchProducts(condition).stream()
                .map(ProductResponseDto::from)
                .toList();
    }

    private boolean isActiveOrSoldOut(Product product) {
        ProductStatus status = product.getStatus();
        return ProductStatus.ACTIVE.equals(status) || ProductStatus.SOLDOUT.equals(status);
    }
}