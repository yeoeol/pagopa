package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepositoryCustom {
    Page<Product> findAll(Pageable pageable);

    List<Product> searchProducts(@NonNull ProductSearchCondition condition);
}
