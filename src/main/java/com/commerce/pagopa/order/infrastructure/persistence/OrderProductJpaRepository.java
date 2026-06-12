package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.repository.OrderProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Long>, OrderProductRepository {
}
