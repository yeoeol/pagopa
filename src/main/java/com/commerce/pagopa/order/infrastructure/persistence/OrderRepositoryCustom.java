package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {
    Page<Order> findAllBySellerId(Long sellerId, Pageable pageable);

    /**
     * 결제 미완료이며 createdAt이 timeoutTime 이하인 주문 조회
     * 자동 취소 스케줄러에서 사용
     */
    List<Order> findUnpaidCreatedBefore(LocalDateTime timeoutTime);
}
