package com.commerce.pagopa.catalog.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.NonNull;

import com.commerce.pagopa.catalog.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductStatus;

public interface ProductRepositoryCustom {
    Page<Product> findAll(Pageable pageable);

    Page<Product> findAllByCategoryOrAncestorCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );

    List<Product> findAllByCursor(
            LocalDateTime cursorCreatedAt,
            Long cursorProductId,
            int limit
    );

    Page<Product> searchProducts(
            @NonNull ProductSearchCondition condition,
            Pageable pageable
    );

    List<Product> findRecommendationCandidatesByKeyword(String keyword, Collection<Long> excludedProductIds, int limit);

    List<Product> findDefaultRecommendationProducts(Collection<Long> excludedProductIds, int limit);
}
