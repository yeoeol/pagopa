package com.commerce.pagopa.domain.admin.product.service;

import com.commerce.pagopa.domain.admin.product.dto.request.ProductStatusChangeRequestDto;
import com.commerce.pagopa.domain.admin.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.entity.enums.ProductStatus;
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
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }

    @Transactional
    public void changeStatus(Long productId, ProductStatusChangeRequestDto requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        ProductStatus status = requestDto.status();

        if (ProductStatus.ACTIVE.equals(status)) {
            product.activate();
        } else if (ProductStatus.INACTIVE.equals(status)) {
            product.inactivate();
        } else if (ProductStatus.SOLDOUT.equals(status)) {
            product.markAsSoldOut();
        } else if (ProductStatus.HIDDEN.equals(status)) {
            product.hide();
        }
    }
}
