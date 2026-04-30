package com.commerce.pagopa.domain.payment.repository;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.payment.entity.Payment;
import com.commerce.pagopa.global.exception.PaymentNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByPaymentKey(String paymentKey);

    default Payment getByOrderOrThrow(Order order) {
        return findByOrder(order).orElseThrow(PaymentNotFoundException::new);
    }

    default Payment getByPaymentKeyOrThrow(String paymentKey) {
        return findByPaymentKey(paymentKey).orElseThrow(PaymentNotFoundException::new);
    }
}
