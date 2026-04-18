package com.commerce.pagopa.domain.admin.product.service;

import com.commerce.pagopa.domain.admin.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAll(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(ProductResponseDto::from);
    }

    @Transactional
    public void active(Long productId) {
        Product product = getProduct(productId);
        product.active();
    }

    @Transactional
    public void inactive(Long productId) {
        Product product = getProduct(productId);
        product.inactive();
    }

    @Transactional
    public void soldOut(Long productId) {
        Product product = getProduct(productId);
        product.soldOut();
    }

    @Transactional
    public void hidden(Long productId) {
        Product product = getProduct(productId);
        product.hidden();
    }

    @Transactional
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
    }
}
