package com.commerce.pagopa.payment.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PaymentCommand(
		@NotNull
		@Positive
		Long orderId,
		@NotBlank
		@Size(max = 50)
		String paymentMethod
) {
}
