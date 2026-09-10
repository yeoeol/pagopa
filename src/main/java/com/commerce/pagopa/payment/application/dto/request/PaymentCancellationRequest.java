package com.commerce.pagopa.payment.application.dto.request;

public record PaymentCancellationRequest(
		String transactionId,
		Integer amount
) {
	public static PaymentCancellationRequest of(String transactionId, Integer amount) {
		return new PaymentCancellationRequest(transactionId, amount);
	}
}
