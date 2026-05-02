package com.commerce.pagopa.admin.category.application.dto.response;

import com.commerce.pagopa.category.domain.model.Category;

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
