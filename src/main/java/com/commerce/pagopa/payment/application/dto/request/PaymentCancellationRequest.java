package com.commerce.pagopa.payment.application.dto.request;

public record PaymentCancellationRequest(
		String transactionId,
		Integer amount,
		String idempotencyKey
) {
	public static PaymentCancellationRequest of(
			String transactionId,
			Integer amount,
			String idempotencyKey
	) {
		return new PaymentCancellationRequest(
				transactionId,
				amount,
				idempotencyKey
		);
	}
}
