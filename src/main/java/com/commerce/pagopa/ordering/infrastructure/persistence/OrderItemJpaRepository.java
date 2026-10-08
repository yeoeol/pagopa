package com.commerce.pagopa.ordering.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.ordering.domain.order.OrderItemRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long>, OrderItemRepository {
}
