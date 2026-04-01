package com.commerce.pagopa.domain.category.dto.response;

import com.commerce.pagopa.domain.category.entity.Category;

public record CategorySimpleResponseDto(
        Long categoryId,
        String name,
        int depth
) {
    public static CategorySimpleResponseDto from(Category category) {
        return new CategorySimpleResponseDto(
                category.getId(),
                category.getName(),
                category.getDepth()
        );
    }
}
