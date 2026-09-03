package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.Payment;

public final class PaymentFixture {

    private PaymentFixture() {
    }

    public static Payment aReadyPayment(Order order) {
        return aPayment(order);
    }

    public static Payment aPaidPayment(Order order) {
        Payment payment = aPayment(order);
        payment.pay("test-payment-key-" + order.getId());
        return payment;
    }

    private static Payment aPayment(Order order) {
        return Payment.create(
                "CREDIT_CARD",
                order.getTotalAmount(),
                order
        );
    }
}
