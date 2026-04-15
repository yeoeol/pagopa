package com.commerce.pagopa.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentApproveRequestDto(
        @NotBlank(message = "결제 키는 필수입니다.")
        String paymentKey,

        @NotBlank(message = "주문 번호는 필수입니다.")
        String orderId,

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive
        BigDecimal amount
) {
}
