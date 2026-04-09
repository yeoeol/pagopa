package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}
