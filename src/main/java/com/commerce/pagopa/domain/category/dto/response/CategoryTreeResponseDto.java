package com.commerce.pagopa.domain.category.dto.response;

import com.commerce.pagopa.domain.category.entity.Category;

import java.util.List;

public record CategoryTreeResponseDto(
        Long categoryId,
        String name,
        int depth,
        List<CategoryTreeResponseDto> children
) {
    public static CategoryTreeResponseDto from(Category category) {
        return new CategoryTreeResponseDto(
                category.getId(),
                category.getName(),
                category.getDepth(),
                category.getChildren().stream()
                        .map(CategoryTreeResponseDto::from)
                        .toList()
        );
    }
}
