package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;

import java.math.BigDecimal;

public final class ProductFixture {

    private ProductFixture() {
    }

    public static Product aProduct(Category category, User seller) {
        return aProduct(category, seller, 10, BigDecimal.ONE);
    }

    public static Product aProduct(Category category, User seller, int stock) {
        return aProduct(category, seller, stock, BigDecimal.ONE);
    }

    public static Product aProduct(Category category, User seller, BigDecimal price) {
        return aProduct(category, seller, 10, price);
    }

    public static Product aProduct(Category category, User seller, int stock, BigDecimal price) {
        return Product.create(
                "test-product",
                "test-description",
                price,
                null,
                stock,
                category,
                seller
        );
    }
}
