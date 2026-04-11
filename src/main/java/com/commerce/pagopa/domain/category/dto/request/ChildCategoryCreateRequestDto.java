package com.commerce.pagopa.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChildCategoryCreateRequestDto(
        @NotNull(message = "{validation.notNull}")
        Long parentId,

        @NotBlank(message = "{validation.notBlank}")
        String name
) {
}
