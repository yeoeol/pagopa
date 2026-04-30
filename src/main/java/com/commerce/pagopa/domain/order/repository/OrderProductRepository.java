package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.global.exception.OrderProductNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query(value =
            "SELECT op " +
            "FROM OrderProduct op " +
                    "JOIN FETCH op.order o " +
                    "JOIN FETCH o.user u " +
                        "WHERE op.id = :id")
    Optional<OrderProduct> findByIdWithOrderAndUser(@Param("id") Long id);

    default OrderProduct getById(Long id) {
        return findById(id).orElseThrow(OrderProductNotFoundException::new);
    }
}
