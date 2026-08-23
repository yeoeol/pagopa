package com.commerce.pagopa.seller.application.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminSellerRejectRequestDto(
		@NotBlank(message = "{validation.notBlank}")
		String reason
) {
}
