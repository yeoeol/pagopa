package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.ORDER_NOT_FOUND;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findAllByPeriod(
            Long userId, OrderStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdForUpdate(Long id);

    default Order findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
    }

    default Order findByIdForUpdateOrThrow(Long id) {
        return findByIdForUpdate(id).orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
    }
}
