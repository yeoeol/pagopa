package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query(value =
            "SELECT DISTINCT o " +
            "FROM Order o " +
                "JOIN o.orderProducts op " +
                "JOIN op.product p " +
            "WHERE p.seller.id = :sellerId")
    Page<Order> findAllByUserId(@Param("sellerId") Long sellerId, Pageable pageable);
}
