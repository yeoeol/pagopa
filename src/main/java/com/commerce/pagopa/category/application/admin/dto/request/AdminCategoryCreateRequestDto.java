package com.commerce.pagopa.category.application.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCategoryCreateRequestDto(
		Long parentId,

		@NotBlank(message = "{validation.notBlank}")
		@Size(max = 50, message = "{validation.max}")
		String name
) {
}
