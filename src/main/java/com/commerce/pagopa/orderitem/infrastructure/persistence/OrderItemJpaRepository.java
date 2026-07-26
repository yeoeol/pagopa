package com.commerce.pagopa.orderitem.infrastructure.persistence;

import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.orderitem.domain.repository.OrderItemRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long>, OrderItemRepository {
}
