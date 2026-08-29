package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.seller.domain.model.Seller;

public final class ProductFixture {

    private ProductFixture() {
    }

    public static Product aProduct(Category category, Seller seller) {
        return aProduct("test-product", "test-description", 1, 10, category, seller);
    }

    public static Product aProduct(Category category, Seller seller, Integer price) {
        return aProduct("test-product", "test-description", price, 10, category, seller);
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
