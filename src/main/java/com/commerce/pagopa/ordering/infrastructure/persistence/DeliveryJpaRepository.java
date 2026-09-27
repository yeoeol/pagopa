package com.commerce.pagopa.ordering.infrastructure.persistence;

import com.commerce.pagopa.ordering.domain.delivery.Delivery;
import com.commerce.pagopa.ordering.domain.delivery.DeliveryRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryJpaRepository extends JpaRepository<Delivery, Long>, DeliveryRepository {
}
