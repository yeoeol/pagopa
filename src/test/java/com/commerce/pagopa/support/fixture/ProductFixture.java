package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.catalog.domain.Category;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.merchant.domain.Seller;

public final class ProductFixture {

    private ProductFixture() {
    }

    public static Product aProduct(Category category, Seller seller) {
        return aProduct("test-product", "test-description", 1, 10, category, seller);
    }

    public static Product aProduct(Category category, Seller seller, Integer stockQuantity) {
        return aProduct("test-product", "test-description", 1000, stockQuantity, category, seller);
    }

    public static Product aProduct(
            String name,
            String description,
            Integer price,
            Integer stockQuantity,
            Category category,
            Seller seller
    ) {
        return Product.create(
                name,
                description,
                price,
                stockQuantity,
                category,
                seller
        );
    }
}
