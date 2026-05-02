package com.commerce.pagopa.order.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderCancelRequestDto(
        @NotBlank(message = "결제 키는 필수입니다.")
        String paymentKey,

        @NotBlank(message = "주문/결제 취소 이유는 필수입니다.")
        String cancelReason
) {
}
