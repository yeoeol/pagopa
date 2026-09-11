package com.commerce.pagopa.payment.domain.model.enums;

import com.commerce.pagopa.global.response.DescribedStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus implements DescribedStatus {
    READY("결제대기"),
    APPROVING("결제승인중"),
    PAID("결제완료"),
    FAILED("결제실패"),
    CANCELLING("결제취소중"),
    CANCELED("결제취소"),
    ;

    private final String description;
}
