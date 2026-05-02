package com.commerce.pagopa.payment.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {

    @Override
    Optional<Payment> findByOrder(Order order);

    @Override
    Optional<Payment> findByOrder_IdAndPaymentKey(Long orderId, String paymentKey);
}
