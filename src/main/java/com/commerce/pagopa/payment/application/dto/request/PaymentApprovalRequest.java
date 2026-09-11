package com.commerce.pagopa.payment.application.dto.request;

public record PaymentApprovalRequest(
		Long orderId,
		Integer amount,
		String paymentMethod,
		String idempotencyKey
) {
	public static PaymentApprovalRequest of(
			Long orderId,
			Integer amount,
			String paymentMethod,
			String idempotencyKey
	) {
		return new PaymentApprovalRequest(
				orderId,
				amount,
				paymentMethod,
				idempotencyKey
		);
	}
}
