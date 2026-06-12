package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.global.exception.OrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findAllByPeriod(
            Long userId, OrderStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    Optional<Order> findByOrderNumber(String orderNumber);

    default Order findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(OrderNotFoundException::new);
    }

    default Order getByOrderNumberOrThrow(String orderNumber) {
        return findByOrderNumber(orderNumber).orElseThrow(OrderNotFoundException::new);
    }
}
