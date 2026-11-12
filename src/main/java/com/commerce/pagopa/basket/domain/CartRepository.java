package com.commerce.pagopa.basket.domain;

import java.util.Optional;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.CART_NOT_FOUND;

public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findById(Long id);

    Optional<Cart> findByUserIdForUpdate(Long userId);

    Optional<Cart> findByUserIdWithItems(Long userId);

    Optional<Cart> findByUserIdWithItemsForUpdate(Long userId);

    default Cart findByUserIdForUpdateOrThrow(Long userId) {
        return findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(CART_NOT_FOUND));
    }
}
