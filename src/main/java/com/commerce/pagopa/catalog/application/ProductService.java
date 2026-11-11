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

import com.commerce.pagopa.catalog.application.dto.request.ProductCursor;
import com.commerce.pagopa.catalog.application.dto.request.ProductCursorRequestDto;
import com.commerce.pagopa.catalog.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.catalog.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;
import com.commerce.pagopa.catalog.domain.ProductStatus;
import com.commerce.pagopa.catalog.event.ProductSearched;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.CursorPageResponseDto;
import com.commerce.pagopa.global.response.OffsetPageResponseDto;

import static com.commerce.pagopa.global.response.ErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public OffsetPageResponseDto<ProductResponseDto> findAllWithActiveAndSoldOut(
            Pageable pageable
    ) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return OffsetPageResponseDto.from(
                productPage.map(ProductResponseDto::from)
        );
    }

    @Transactional(readOnly = true)
    public ProductResponseDto find(Long productId) {
        Product product = productRepository.findByIdWithCategoryParentsAndSellerAndProductImages(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        return ProductResponseDto.from(product);
    }

    @Transactional(readOnly = true)
    public OffsetPageResponseDto<ProductResponseDto> findAllByCategory(Long categoryId, Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByCategoryOrAncestorCategoryIdAndStatusIn(
                categoryId,
                List.of(
                        ProductStatus.ACTIVE,
                        ProductStatus.SOLD_OUT
                ),
                pageable
        );
        return OffsetPageResponseDto.from(
                productPage.map(ProductResponseDto::from)
        );
    }

    @Transactional(readOnly = true)
    public CursorPageResponseDto<ProductResponseDto> findAllByCursor(ProductCursorRequestDto requestDto) {
        int size = requestDto.resolvedSize();
        ProductCursor cursor = ProductCursor.decodeNullable(requestDto.cursor());

        List<Product> fetchedProducts = productRepository.findAllByCursor(
                cursor == null ? null : cursor.createdAt(),
                cursor == null ? null : cursor.productId(),
                size + 1
        );

        boolean hasNext = fetchedProducts.size() > size;

        List<Product> products = fetchedProducts.stream()
                .limit(size)
                .toList();

        String nextCursor = createNextCursor(
                products,
                hasNext
        );

        return CursorPageResponseDto.from(
                products.stream()
                        .map(ProductResponseDto::from)
                        .toList(),
                nextCursor,
                hasNext
        );
    }

    @Transactional
    public OffsetPageResponseDto<ProductResponseDto> search(
            Long userId,
            String sessionId,
            @NonNull ProductSearchCondition condition,
            Pageable pageable
    ) {
        Page<Product> productPage = productRepository
                .searchProducts(condition, pageable);

        if (userId != null && sessionId == null) {
            events.publishEvent(
                    new ProductSearched(
                            UUID.randomUUID(),
                            userId,
                            sessionId,
                            condition.productName(),
                            LocalDateTime.now()
                    )
            );
        }

        return OffsetPageResponseDto.from(
                productPage.map(ProductResponseDto::from)
        );
    }

    private String createNextCursor(
            List<Product> products,
            boolean hasNext
    ) {
        if (!hasNext || products.isEmpty()) {
            return null;
        }

        Product lastProduct = products.getLast();

        return new ProductCursor(
                lastProduct.getCreatedAt(),
                lastProduct.getId()
        ).encode();
    }
}
