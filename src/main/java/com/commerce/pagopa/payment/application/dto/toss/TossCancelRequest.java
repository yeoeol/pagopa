package com.commerce.pagopa.payment.application.dto.toss;

public record TossCancelRequest(
        String cancelReason,
        Integer cancelAmount
) {
}
