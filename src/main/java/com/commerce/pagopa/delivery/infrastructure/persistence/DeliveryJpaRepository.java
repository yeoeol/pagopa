package com.commerce.pagopa.delivery.infrastructure.persistence;

import com.commerce.pagopa.delivery.domain.model.Delivery;
import com.commerce.pagopa.delivery.domain.repository.DeliveryRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryJpaRepository extends JpaRepository<Delivery, Long>, DeliveryRepository {
}
