package com.commerce.pagopa.payment.application.dto.request;

public record PaymentCancellationRequest(
        Long paymentId,
        String providerPaymentId,
        Integer amount,
        String idempotencyKey,
        String cancelReason
) {
    public static PaymentCancellationRequest of(
            Long paymentId,
            String providerPaymentId,
            Integer amount,
            String idempotencyKey,
            String cancelReason
    ) {
        return new PaymentCancellationRequest(
                paymentId,
                providerPaymentId,
                amount,
                idempotencyKey,
                cancelReason
        );
    }
}
