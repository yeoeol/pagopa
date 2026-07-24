package com.commerce.pagopa.category.application.dto.response;

import com.commerce.pagopa.category.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public record CategoryTreeResponseDto(
        Long categoryId,
        Long parentId,
        String name,
        List<CategoryTreeResponseDto> children
) {
    public static CategoryTreeResponseDto init(Category category) {
        return new CategoryTreeResponseDto(
                category.getId(),
                category.getParent().getId(),
                category.getName(),
                new ArrayList<>()
        );
    }

    public void addChild(CategoryTreeResponseDto child) {
        children.add(child);
    }
}
