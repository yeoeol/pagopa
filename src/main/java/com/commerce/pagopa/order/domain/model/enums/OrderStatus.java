package com.commerce.pagopa.order.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING_PAYMENT("결제대기"),
    CONFIRMED("주문확정"),
    COMPLETED("주문완료"),
    CANCELED("주문취소"),
    ;

    private final String description;
}
