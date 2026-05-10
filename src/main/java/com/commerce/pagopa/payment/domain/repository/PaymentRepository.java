package com.commerce.pagopa.payment.domain.repository;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.global.exception.PaymentNotFoundException;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByOrder_IdAndPaymentKey(Long orderId, String paymentKey);

    default Payment getByOrderOrThrow(Order order) {
        return findByOrder(order).orElseThrow(PaymentNotFoundException::new);
    }

    default Payment getByIdOrThrow(Long paymentId) {
        return findById(paymentId).orElseThrow(PaymentNotFoundException::new);
    }

    default Payment getByOrderIdAndPaymentKeyOrThrow(Long orderId, String paymentKey) {
        return findByOrder_IdAndPaymentKey(orderId, paymentKey).orElseThrow(PaymentNotFoundException::new);
    }
}
