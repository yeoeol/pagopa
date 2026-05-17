package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.category.domain.model.Category;

public final class CategoryFixture {

    private CategoryFixture() {
    }

    public static Category aRoot() {
        return Category.createRoot("root");
    }

    // root → mid → leaf 3-level tree
    public static CategoryTree aTree() {
        Category root = Category.createRoot("root");
        Category mid = root.createChild("mid");
        Category leaf = mid.createChild("leaf");
        return new CategoryTree(root, leaf);
    }

    public record CategoryTree(Category root, Category leaf) {
    }
}
