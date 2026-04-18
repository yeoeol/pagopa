package com.commerce.pagopa.domain.admin.category.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RootCategoryCreateRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String name
) {
}
