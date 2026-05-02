package com.commerce.pagopa.admin.product.application;

import com.commerce.pagopa.admin.product.application.dto.request.ProductStatusChangeRequestDto;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
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

    @Transactional(readOnly = true)
    public ProductResponseDto find(Long productId) {
        Product product = productRepository.findByIdOrThrow(productId);
        return ProductResponseDto.from(product);
    }

    @Transactional
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }

    @Transactional
    public void changeStatus(Long productId, ProductStatusChangeRequestDto requestDto) {
        Product product = productRepository.findByIdOrThrow(productId);
        ProductStatus status = requestDto.status();

        switch (status) {
            case ACTIVE -> product.activate();
            case INACTIVE -> product.inactivate();
            case SOLDOUT -> product.markAsSoldOut();
            case HIDDEN -> product.hide();
        }
    }
}
