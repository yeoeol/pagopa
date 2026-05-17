package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;

import java.math.BigDecimal;

public final class ProductFixture {

    private ProductFixture() {
    }

    public static Product aProduct(Category category, User seller) {
        return aProduct("test-product", "test-description", category, seller, 10, BigDecimal.ONE);
    }

    public static Product aProduct(Category category, User seller, int stock) {
        return aProduct("test-product", "test-description", category, seller, stock, BigDecimal.ONE);
    }

    public static Product aProduct(Category category, User seller, BigDecimal price) {
        return aProduct("test-product", "test-description", category, seller, 10, price);
    }

    public static Product aProduct(String name, String description, Category category, User seller, int stock, BigDecimal price) {
        return Product.create(
                name,
                description,
                price,
                null,
                stock,
                category,
                seller
        );
    }
}
