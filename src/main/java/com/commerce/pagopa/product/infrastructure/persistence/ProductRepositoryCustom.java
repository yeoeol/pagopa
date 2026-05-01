package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> searchProducts(ProductSearchCondition condition);
}
