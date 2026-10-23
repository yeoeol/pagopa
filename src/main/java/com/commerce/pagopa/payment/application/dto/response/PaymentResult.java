package com.commerce.pagopa.payment.application.dto.response;

import java.time.LocalDateTime;

import com.commerce.pagopa.payment.domain.Payment;
import com.commerce.pagopa.payment.domain.PaymentProvider;

public record PaymentResult(
        Long paymentId,
        String provider,
        String providerPaymentId,
        Long orderId,
        String status,
        String paymentMethod,
        Integer amount,
        LocalDateTime approvedAt,
        LocalDateTime canceledAt
) {
    public static PaymentResult from(Payment payment) {
        return new PaymentResult(
                payment.getId(),
                payment.getProvider().name(),
                payment.getProviderPaymentId(),
                payment.getOrderId(),
                payment.getStatus().name(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getPaidAt(),
                payment.getCanceledAt()
        );
    }

    public static PaymentResult approve(
            Long paymentId,
            PaymentProvider provider,
            String providerPaymentId,
            String status,
            String paymentMethod,
            Integer amount,
            LocalDateTime paidAt
    ) {
        return new PaymentResult(
                paymentId,
                provider.name(),
                providerPaymentId,
                null,
                status,
                paymentMethod,
                amount,
                paidAt,
                null
        );
    }

    public static PaymentResult cancel(
            Long paymentId,
            PaymentProvider provider,
            String providerPaymentId,
            String status,
            String paymentMethod,
            Integer amount,
            LocalDateTime canceledAt
    ) {
        return new PaymentResult(
                paymentId,
                provider.name(),
                providerPaymentId,
                null,
                status,
                paymentMethod,
                amount,
                null,
                canceledAt
        );
    }

    public PaymentResult withCancellation(
            LocalDateTime canceledAt
    ) {
        return new PaymentResult(
                this.paymentId,
                this.provider,
                this.providerPaymentId,
                this.orderId,
                "CANCELED",
                this.paymentMethod,
                this.amount,
                this.approvedAt,
                canceledAt
        );
    }
}
