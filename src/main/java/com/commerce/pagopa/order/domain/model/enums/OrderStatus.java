package com.commerce.pagopa.order.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDERED("주문생성(결제대기)"), // 주문은 생성되었으나 아직 결제되지 않은 상태
    PAID("결제완료"),
    DELIVERING("배송중"),
    COMPLETED("배송완료"),
    CANCELLED("주문취소(결제실패/취소)"),
    ;

    private final String description;
}
