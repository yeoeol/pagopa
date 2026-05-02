package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
    Page<Order> findAllBySellerId(Long sellerId, Pageable pageable);
}
