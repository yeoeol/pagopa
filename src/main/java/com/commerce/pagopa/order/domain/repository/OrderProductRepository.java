package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.global.exception.OrderProductNotFoundException;
import com.commerce.pagopa.order.domain.model.OrderProduct;

import java.util.Optional;

public interface OrderProductRepository {

    Optional<OrderProduct> findById(Long id);

    default OrderProduct findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(OrderProductNotFoundException::new);
    }
}
