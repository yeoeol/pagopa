package com.commerce.pagopa.domain.admin.category.dto.response;

import com.commerce.pagopa.domain.category.entity.Category;

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
