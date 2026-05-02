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

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 특정 상태(ORDERED)이면서 지정된 시간(created_at) 이하(이전 포함) 생성된 주문들을 조회
     */
    List<Order> findByStatusAndCreatedAtLessThanEqual(OrderStatus status, LocalDateTime dateTime);

    Page<Order> findAllBySellerId(Long sellerId, Pageable pageable);

    default Order findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(OrderNotFoundException::new);
    }

    default Order getByOrderNumberOrThrow(String orderNumber) {
        return findByOrderNumber(orderNumber).orElseThrow(OrderNotFoundException::new);
    }
}
