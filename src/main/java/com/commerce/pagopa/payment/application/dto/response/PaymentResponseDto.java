package com.commerce.pagopa.payment.application.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;

import java.time.Instant;

public record PaymentResponseDto(
        Long paymentId,
        String paymentMethod,
        Integer amount,
        StatusResponseDto<PaymentStatus> status,
        Instant paidAt,
        Instant canceledAt,
        String paymentKey,
        OrderResponseDto order
) {
    public static PaymentResponseDto from(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                StatusResponseDto.from(payment.getStatus()),
                payment.getPaidAt(),
                payment.getCanceledAt(),
                payment.getPaymentKey(),
                OrderResponseDto.from(payment.getOrder())
        );
    }
}
