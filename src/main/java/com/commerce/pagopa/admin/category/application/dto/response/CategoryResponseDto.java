package com.commerce.pagopa.admin.category.application.dto.response;

import com.commerce.pagopa.category.domain.model.Category;

public record CategoryResponseDto(
        Long categoryId,
        String name,
        int depth,
        CategoryResponseDto parent
) {
    public static CategoryResponseDto from(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDepth(),
                from(category.getParent())
        );
    }
}
