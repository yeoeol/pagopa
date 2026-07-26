package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface OrderRepositoryCustom {

    Page<Order> findAllByPeriod(
            Long userId, OrderStatus status, Instant start, Instant end, Pageable pageable
    );
}
