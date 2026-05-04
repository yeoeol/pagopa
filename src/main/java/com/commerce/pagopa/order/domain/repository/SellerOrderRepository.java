package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.global.exception.SellerOrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SellerOrderRepository {

    SellerOrder save(SellerOrder sellerOrder);

    Optional<SellerOrder> findById(Long id);

    Page<SellerOrder> findBySellerId(Long sellerId, Pageable pageable);

    Optional<SellerOrder> findByIdAndSellerId(Long id, Long sellerId);

    default SellerOrder findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(SellerOrderNotFoundException::new);
    }

    default SellerOrder getByIdAndSellerIdOrThrow(Long id, Long sellerId) {
        return findByIdAndSellerId(id, sellerId).orElseThrow(SellerOrderNotFoundException::new);
    }
}
