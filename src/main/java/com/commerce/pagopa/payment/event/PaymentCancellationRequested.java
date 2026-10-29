package com.commerce.pagopa.payment.event;

import java.util.UUID;

import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;

public record PaymentCancellationRequested(
        UUID eventId,
        Long paymentId,
        String providerPaymentId,
        Integer amount,
        String cancelReason
) {
    public String idempotencyKey() {
        return "payment-cancellation-" + eventId;
    }

    public PaymentCancellationRequest toRequest() {
        return PaymentCancellationRequest.of(
                paymentId,
                providerPaymentId,
                amount,
                idempotencyKey(),
                cancelReason
        );
    }
}
