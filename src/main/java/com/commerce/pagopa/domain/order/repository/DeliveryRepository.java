package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}