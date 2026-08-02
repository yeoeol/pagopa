package com.commerce.pagopa.orderitem.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;

import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.ORDER_ITEM_NOT_FOUND;

public interface OrderItemRepository {

    Optional<OrderItem> findById(Long id);

    default OrderItem findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(ORDER_ITEM_NOT_FOUND));
    }
}
