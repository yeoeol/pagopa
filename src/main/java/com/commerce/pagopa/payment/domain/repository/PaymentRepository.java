package com.commerce.pagopa.payment.domain.repository;

import com.commerce.pagopa.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);
}
