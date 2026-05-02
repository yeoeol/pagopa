package com.commerce.pagopa.admin.category.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String name
) {
}
