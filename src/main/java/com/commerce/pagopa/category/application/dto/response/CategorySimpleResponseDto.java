package com.commerce.pagopa.category.application.dto.response;

import com.commerce.pagopa.category.domain.model.Category;

public record CategorySimpleResponseDto(
        Long categoryId,
        Long parentId,
        String name
) {
    public static CategorySimpleResponseDto from(Category category) {
        return new CategorySimpleResponseDto(
                category.getId(),
                category.getParent().getId(),
                category.getName()
        );
    }
}
