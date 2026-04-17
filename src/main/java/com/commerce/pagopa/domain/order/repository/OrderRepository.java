package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    
    Optional<Order> findByOrderNumber(String orderNumber);
}
