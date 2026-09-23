package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.catalog.domain.Category;
import com.commerce.pagopa.catalog.domain.Product;

public final class ProductFixture {

    private ProductFixture() {
    }

    public static Product aProduct(Category category, Long sellerId) {
        return aProduct("test-product", "test-description", 1, 10, category, sellerId);
    }

    public static Product aProduct(Category category, Long sellerId, Integer stockQuantity) {
        return aProduct("test-product", "test-description", 1000, stockQuantity, category, sellerId);
    }

    public static Product aProduct(
            String name,
            String description,
            Integer price,
            Integer stockQuantity,
            Category category,
            Long sellerId
    ) {
        return Product.create(
                name,
                description,
                price,
                stockQuantity,
                category,
                sellerId
        );
    }
}
