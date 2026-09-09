package com.commerce.pagopa.payment.application.dto.request;

public record PaymentCommand(
		Long userId,
		Long orderId,
		String paymentMethod
) {
}
