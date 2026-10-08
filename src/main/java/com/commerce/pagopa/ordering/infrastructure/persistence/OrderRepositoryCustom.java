package com.commerce.pagopa.ordering.infrastructure.persistence;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderStatus;

public interface OrderRepositoryCustom {

    Page<Order> findAllByPeriod(
            Long userId,
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}
