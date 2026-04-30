package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.global.exception.OrderNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 특정 상태(ORDERED)이면서 지정된 시간(created_at) 이하(이전 포함) 생성된 주문들을 조회
     */
    List<Order> findByStatusAndCreatedAtLessThanEqual(OrderStatus status, LocalDateTime dateTime);

    default Order findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(OrderNotFoundException::new);
    }

    default Order getByOrderNumberOrThrow(String orderNumber) {
        return findByOrderNumber(orderNumber).orElseThrow(OrderNotFoundException::new);
    }
}
