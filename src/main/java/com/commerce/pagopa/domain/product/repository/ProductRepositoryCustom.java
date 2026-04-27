package com.commerce.pagopa.domain.product.repository;

import com.commerce.pagopa.domain.product.dto.request.ProductSearchCondition;
import com.commerce.pagopa.domain.product.entity.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> searchProducts(ProductSearchCondition productSearchCondition);
}
