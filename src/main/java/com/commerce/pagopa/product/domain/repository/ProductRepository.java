package com.commerce.pagopa.product.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;

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

    Page<Product> findAllBySellerId(Long userId, Pageable pageable);

    Page<Product> findAllByCategoryOrAncestorCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );

    List<Product> searchProducts(@NonNull ProductSearchCondition condition);

    List<Product> findAllByIdIn(List<Long> productIds);

    Optional<Product> findByIdForUpdate(Long id);

    default Product findByIdForUpdateOrThrow(Long id) {
        return findByIdForUpdate(id).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }

    default Product findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }
}
