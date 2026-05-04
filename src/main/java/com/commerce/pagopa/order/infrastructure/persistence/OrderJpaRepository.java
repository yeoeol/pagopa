package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long>, OrderRepository, OrderRepositoryCustom {

    @Override
    Page<Order> findAllByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    @Override
    Optional<Order> findByOrderNumber(String orderNumber);
}
