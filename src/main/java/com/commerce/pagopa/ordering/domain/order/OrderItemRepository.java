package com.commerce.pagopa.ordering.domain.order;

import java.util.Optional;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.ORDER_ITEM_NOT_FOUND;

public interface OrderItemRepository {

    Optional<OrderItem> findById(Long id);

    default OrderItem findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(ORDER_ITEM_NOT_FOUND));
    }
}
