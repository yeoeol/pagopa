package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.global.exception.SellerOrderNotFoundException;

import java.util.Optional;

public interface SellerOrderRepository {

    SellerOrder save(SellerOrder sellerOrder);

    Optional<SellerOrder> findById(Long id);

    Optional<SellerOrder> findBySellerIdAndOrderId(Long sellerId, Long orderId);

    default SellerOrder findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(SellerOrderNotFoundException::new);
    }

    default SellerOrder getBySellerIdAndOrderIdOrThrow(Long sellerId, Long orderId) {
        return findBySellerIdAndOrderId(sellerId, orderId).orElseThrow(SellerOrderNotFoundException::new);
    }
}
