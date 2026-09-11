package com.commerce.pagopa.payment.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentCommand(
		@NotNull(message = "{validation.notnull}")
		Long orderId,

		@NotBlank(message = "{validation.notblank}")
		@Size(min = 1, max = 50, message = "{validation.size}")
		String paymentMethod
) {
}
