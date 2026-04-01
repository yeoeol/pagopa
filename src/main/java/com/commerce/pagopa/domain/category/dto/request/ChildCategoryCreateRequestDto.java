package com.commerce.pagopa.domain.category.dto.request;

public record ChildCategoryCreateRequestDto(
        Long parentId,
        String name
) {
}
