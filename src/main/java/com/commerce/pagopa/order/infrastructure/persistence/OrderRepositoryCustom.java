package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderRepositoryCustom {

    Page<Order> findAllByPeriod(
            Long userId, OrderStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable
    );
}
