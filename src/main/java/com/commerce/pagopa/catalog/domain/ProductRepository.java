package com.commerce.pagopa.catalog.domain;

import com.commerce.pagopa.catalog.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.global.exception.BusinessException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;

import static com.commerce.pagopa.global.response.ErrorCode.PRODUCT_NOT_FOUND;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    Optional<Product> findByIdWithCategoryParentsAndSellerAndProductImages(Long productId);

    List<Product> findAll();

    Page<Product> findAll(Pageable pageable);

    boolean existsById(Long id);

    void deleteById(Long id);

    Page<Product> findAllByUserId(Long userId, Pageable pageable);

    Page<Product> findAllByCategoryOrAncestorCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );

    List<Product> searchProducts(@NonNull ProductSearchCondition condition);

    List<Product> findByIdIn(Collection<Long> productIds);

    Optional<Product> findByIdForUpdate(Long id);

	List<Product> findRecommendationCandidatesByKeyword(
			String keyword,
			Collection<Long> excludedProductIds,
			int limit
	);

	List<Product> findDefaultRecommendationProducts(
			Collection<Long> excludedProductIds,
			int limit
	);

    default Product findByIdForUpdateOrThrow(Long id) {
        return findByIdForUpdate(id).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }

    default Product findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }
}
