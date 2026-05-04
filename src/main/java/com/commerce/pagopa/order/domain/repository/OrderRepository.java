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

    Page<Order> findAllByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 결제 미완료이며 createdAt이 timeoutTime 이하인 주문 조회
     * 자동 취소 스케줄러에서 사용
     */
    List<Order> findUnpaidCreatedBefore(LocalDateTime timeoutTime);

    default Order findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(OrderNotFoundException::new);
    }

    default Order getByOrderNumberOrThrow(String orderNumber) {
        return findByOrderNumber(orderNumber).orElseThrow(OrderNotFoundException::new);
    }
}
