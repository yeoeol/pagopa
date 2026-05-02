package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.repository.OrderProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Long>, OrderProductRepository {

    @Override
    @Query(value =
            "SELECT op " +
            "FROM OrderProduct op " +
                    "JOIN FETCH op.order o " +
                    "JOIN FETCH o.user u " +
                        "WHERE op.id = :id")
    Optional<OrderProduct> findByIdWithOrderAndUser(@Param("id") Long id);
}
