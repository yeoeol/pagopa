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

    /**
     * status가 PAID 또는 PARTIAL_CANCELLED 일 때만 CANCELLING으로 전이하고, 영향받은 행(row) 수를 반환
     * 반환 1 = 진행권 획득 성공, 0 = 이미 다른 트랜잭션이 점유했거나 취소 불가 상태
     */
    int acquireCancelLock(Long paymentId);

    /**
     * CANCELLING 점유 상태를 직전 상태(PAID)로 되돌림
     */
    int releaseCancelLock(Long paymentId);

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
