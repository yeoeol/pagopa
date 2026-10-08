package com.commerce.pagopa.ordering.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.ordering.domain.delivery.Delivery;
import com.commerce.pagopa.ordering.domain.delivery.DeliveryRepository;

public interface DeliveryJpaRepository extends JpaRepository<Delivery, Long>, DeliveryRepository {
}
