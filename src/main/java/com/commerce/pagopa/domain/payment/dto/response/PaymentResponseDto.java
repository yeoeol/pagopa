package com.commerce.pagopa.domain.payment.dto.response;

import java.math.BigDecimal;

public record PaymentResponseDto(
        String orderId,     // 백엔드의 orderNumber
        String orderName,   // 대표 상품명 등
        String successUrl,  // 성공 리다이렉트 URL
        String failUrl,     // 실패 리다이렉트 URL
        BigDecimal amount,  // 결제 금액
        String customerName // 주문자명
) {
    public static PaymentResponseDto of(String orderId, String orderName, BigDecimal amount, String customerName) {
        return new PaymentResponseDto(
                orderId,
                orderName,
                "http://localhost:3000/success",
                "http://localhost:3000/fail",
                amount,
                customerName
        );
    }
}
