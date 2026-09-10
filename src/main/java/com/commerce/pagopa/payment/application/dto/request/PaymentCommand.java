package com.commerce.pagopa.payment.application.dto.request;

public record PaymentCommand(
		Long orderId,
		String paymentMethod
) {
}
