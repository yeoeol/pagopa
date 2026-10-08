package com.commerce.pagopa.ordering.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.DescribedStatus;

@Getter
@RequiredArgsConstructor
public enum OrderStatus implements DescribedStatus {
    PENDING_PAYMENT("결제대기"), CONFIRMED("주문확정"), COMPLETED("주문완료"), CANCELED("주문취소"),;

    private final String description;
}
