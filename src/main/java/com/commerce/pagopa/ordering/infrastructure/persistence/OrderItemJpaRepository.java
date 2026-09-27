package com.commerce.pagopa.ordering.infrastructure.persistence;

import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.ordering.domain.order.OrderItemRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long>, OrderItemRepository {
}
