package com.commerce.pagopa.catalog.infrastructure.persistence;

import com.commerce.pagopa.catalog.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

import lombok.NonNull;

public interface ProductRepositoryCustom {
    Page<Product> findAll(Pageable pageable);

    Page<Product> findAllByCategoryOrAncestorCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );

    List<Product> searchProducts(@NonNull ProductSearchCondition condition);

    List<Product> findRecommendationCandidatesByKeyword(
            String keyword,
            Collection<Long> excludedProductIds,
            int limit
    );

    List<Product> findDefaultRecommendationProducts(
            Collection<Long> excludedProductIds,
            int limit
    );
}
