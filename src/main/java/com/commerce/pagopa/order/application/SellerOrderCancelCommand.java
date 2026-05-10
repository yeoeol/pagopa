package com.commerce.pagopa.order.application;

import com.commerce.pagopa.payment.domain.model.Payment;

import java.math.BigDecimal;

record SellerOrderCancelCommand(
        Long orderId,
        Long sellerOrderId,
        Payment payment,
        BigDecimal cancelAmount
) {
}
