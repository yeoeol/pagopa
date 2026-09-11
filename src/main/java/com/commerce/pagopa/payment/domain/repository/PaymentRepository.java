package com.commerce.pagopa.payment.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);

    Optional<Payment> findByIdForUpdate(Long paymentId);

    Optional<Payment> findByOrderId(Long orderId);

    default Payment findByIdOrThrow(Long paymentId) {
        return findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    default Payment findByIdForUpdateOrThrow(Long paymentId) {
        return findByIdForUpdate(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}
