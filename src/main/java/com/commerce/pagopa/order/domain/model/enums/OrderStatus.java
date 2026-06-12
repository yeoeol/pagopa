package com.commerce.pagopa.order.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDERED("주문완료"),
    DELIVERING("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("주문취소"),
    ;

    private final String description;
}
