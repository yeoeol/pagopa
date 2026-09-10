package com.commerce.pagopa.payment.infrastructure.persistence;

import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Payment p WHERE p.id = :paymentId")
	Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);
}
