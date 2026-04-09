package com.commerce.pagopa.domain.order.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDERED("주문완료"),
    PAID("결제완료"),
    DELIVERING("배송중"),
    COMPLETED("배송완료"),
    CANCELLED("주문취소"),
    ;

    private final String description;
}
