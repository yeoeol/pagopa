package com.commerce.pagopa.payment.application.dto.request;

public record PaymentApprovalRequest(
        Long paymentId,
        Long orderId,
        String providerPaymentId,
        String providerOrderId,
        Integer amount,
        String paymentMethod,
        String idempotencyKey
) {
    public static PaymentApprovalRequest of(
            Long paymentId,
            Long orderId,
            String providerPaymentId,
            String providerOrderId,
            Integer amount,
            String paymentMethod,
            String idempotencyKey
    ) {
        return new PaymentApprovalRequest(
                paymentId,
                orderId,
                providerPaymentId,
                providerOrderId,
                amount,
                paymentMethod,
                idempotencyKey
        );
    }
}
