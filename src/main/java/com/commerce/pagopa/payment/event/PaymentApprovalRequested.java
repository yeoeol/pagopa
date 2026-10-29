package com.commerce.pagopa.payment.event;

import java.util.UUID;

import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;

public record PaymentApprovalRequested(
        UUID eventId,
        Long paymentId,
        Long orderId,
        String providerPaymentId,
        String providerOrderId,
        Integer amount,
        String paymentMethod
) {
    public String idempotencyKey() {
        return "payment-approval-" + eventId;
    }

    public PaymentApprovalRequest toRequest() {
        return PaymentApprovalRequest.of(
                paymentId,
                orderId,
                providerPaymentId,
                providerOrderId,
                amount,
                paymentMethod,
                idempotencyKey()
        );
    }
}
