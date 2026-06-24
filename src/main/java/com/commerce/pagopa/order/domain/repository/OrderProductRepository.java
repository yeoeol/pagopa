package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.order.domain.model.OrderProduct;

import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.ORDER_PRODUCT_NOT_FOUND;

public interface OrderProductRepository {

    Optional<OrderProduct> findById(Long id);

    default OrderProduct findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(ORDER_PRODUCT_NOT_FOUND));
    }
}
