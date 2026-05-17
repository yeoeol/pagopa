package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.Payment;

public final class PaymentFixture {

    private PaymentFixture() {
    }

    public static Payment aReadyPayment(Order order) {
        return Payment.create(order);
    }

    public static Payment aPaidPayment(Order order) {
        Payment payment = Payment.create(order);
        payment.markInProgress();
        payment.success("test-payment-key-" + order.getOrderNumber());
        return payment;
    }
}
