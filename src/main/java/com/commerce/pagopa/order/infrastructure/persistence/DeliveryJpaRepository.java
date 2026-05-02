package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.repository.DeliveryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryJpaRepository extends JpaRepository<Delivery, Long>, DeliveryRepository {
}
