package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
