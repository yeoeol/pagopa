package com.commerce.pagopa.catalog.application.dto.response;

import com.commerce.pagopa.catalog.domain.Category;

public record CategorySimpleResponseDto(
        Long categoryId,
        Long parentId,
        String name
) {
    public static CategorySimpleResponseDto from(Category category) {
        return new CategorySimpleResponseDto(
                category.getId(),
                category.getParent() == null
                        ? null
                        : category.getParent()
                                .getId(),
                category.getName()
        );
    }
}
