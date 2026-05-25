package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {

    /**
     * 결제 미완료이며 createdAt이 timeoutTime 이하인 주문 조회 (오래된 순 limit건)
     * 자동 취소 스케줄러의 청크 폴링용
     */
    List<Order> findUnpaidCreatedBefore(LocalDateTime timeoutTime, int limit);

    Page<Order> findAllByPeriod(
            Long userId, OrderStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable
    );
}
