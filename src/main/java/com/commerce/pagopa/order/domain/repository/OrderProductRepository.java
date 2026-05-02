package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.global.exception.OrderProductNotFoundException;

import java.util.Optional;

public interface OrderProductRepository {

    Optional<OrderProduct> findById(Long id);

    Optional<OrderProduct> findByIdWithOrderAndUser(Long id);

    default OrderProduct getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(OrderProductNotFoundException::new);
    }
}
