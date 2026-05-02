package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long>, OrderRepository, OrderRepositoryCustom {

    @Override
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Override
    Optional<Order> findByOrderNumber(String orderNumber);

    @Override
    List<Order> findByStatusAndCreatedAtLessThanEqual(OrderStatus status, LocalDateTime dateTime);
}
