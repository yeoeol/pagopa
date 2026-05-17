package com.commerce.pagopa.payment.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {

    @Override
    Optional<Payment> findByOrder(Order order);

    @Override
    Optional<Payment> findByOrder_IdAndPaymentKey(Long orderId, String paymentKey);

    @Override
    default int acquireCancelLock(Long paymentId) {
        return acquireCancelLockInternal(
                paymentId,
                PaymentStatus.PAID,
                PaymentStatus.PARTIAL_CANCELLED,
                PaymentStatus.CANCELLING
        );
    }

    @Override
    default int releaseCancelLock(Long paymentId) {
        return releaseCancelLockInternal(
                paymentId,
                PaymentStatus.PAID,
                PaymentStatus.PARTIAL_CANCELLED,
                PaymentStatus.CANCELLING
        );
    }

    @Modifying
    @Query("""
            UPDATE Payment p
               SET p.status = :cancelling
             WHERE p.id = :paymentId
               AND p.status IN (:paid, :partialCancelled)
            """)
    int acquireCancelLockInternal(@Param("paymentId") Long paymentId,
                                  @Param("paid") PaymentStatus paid,
                                  @Param("partialCancelled") PaymentStatus partialCancelled,
                                  @Param("cancelling") PaymentStatus cancelling);

    @Modifying
    @Query("""
            UPDATE Payment p
               SET p.status = CASE
                                 WHEN p.cancelledAmount = 0 THEN :paid
                                 ELSE :partialCancelled
                              END
             WHERE p.id = :paymentId
               AND p.status = :cancelling
            """)
    int releaseCancelLockInternal(@Param("paymentId") Long paymentId,
                                  @Param("paid") PaymentStatus paid,
                                  @Param("partialCancelled") PaymentStatus partialCancelled,
                                  @Param("cancelling") PaymentStatus cancelling);
}
