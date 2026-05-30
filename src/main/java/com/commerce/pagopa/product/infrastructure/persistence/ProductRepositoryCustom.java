package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ProductRepositoryCustom {
    Page<Product> findAll(Pageable pageable);

    Page<Product> findAllByCategoryOrAncestorCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );

    List<Product> searchProducts(@NonNull ProductSearchCondition condition);
}
