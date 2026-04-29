package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query(value =
            "SELECT DISTINCT o " +
            "FROM Order o " +
                "JOIN FETCH o.orderProducts op " +
                "JOIN FETCH op.product p " +
            "WHERE p.seller.id = :sellerId")
    Page<Order> findAllByUserId(@Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * 특정 상태(ORDERED)이면서 지정된 시간(created_at) 이하(이전 포함) 생성된 주문들을 조회
     */
    List<Order> findByStatusAndCreatedAtLessThanEqual(OrderStatus status, LocalDateTime dateTime);
}
