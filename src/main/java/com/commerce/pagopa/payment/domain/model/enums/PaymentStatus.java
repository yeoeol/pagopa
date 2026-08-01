package com.commerce.pagopa.payment.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제대기"),
    PAID("결제완료"),
    FAILED("결제실패"),
    CANCELED("결제취소"),
    ;

    private final String description;
}
