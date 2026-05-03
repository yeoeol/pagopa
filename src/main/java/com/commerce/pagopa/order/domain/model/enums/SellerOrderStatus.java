package com.commerce.pagopa.order.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerOrderStatus {
    PENDING_PAYMENT("결제 대기"), // 주문 생성 후 결제 승인 전
    READY("출고 준비"),            // 결제 완료, 셀러가 발송 준비 중
    DELIVERING("배송중"),
    COMPLETED("배송완료"),
    CANCELLED("취소됨"),
    ;

    private final String description;
}
