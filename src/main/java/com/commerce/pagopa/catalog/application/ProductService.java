package com.commerce.pagopa.catalog.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.catalog.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;
import com.commerce.pagopa.catalog.domain.ProductStatus;
import com.commerce.pagopa.catalog.event.ProductSearched;
import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllWithActiveAndSoldOut(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(ProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto find(Long productId) {
        Product product = productRepository.findByIdWithCategoryParentsAndSellerAndProductImages(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        return ProductResponseDto.from(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllByCategory(Long categoryId, Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByCategoryOrAncestorCategoryIdAndStatusIn(
                categoryId,
                List.of(
                        ProductStatus.ACTIVE,
                        ProductStatus.SOLD_OUT
                ),
                pageable
        );
        return productPage.map(ProductResponseDto::from);
    }

    @Transactional
    public List<ProductResponseDto> search(
            Long userId,
            String sessionId,
            @NonNull ProductSearchCondition condition
    ) {
        List<ProductResponseDto> products = productRepository.searchProducts(condition)
                .stream()
                .map(ProductResponseDto::from)
                .toList();

        events.publishEvent(
                new ProductSearched(
                        UUID.randomUUID(),
                        userId,
                        sessionId,
                        condition.productName(),
                        LocalDateTime.now()
                )
        );

        return products;
    }
}
