package com.commerce.pagopa.payment.infrastructure.persistence;

import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {
}
