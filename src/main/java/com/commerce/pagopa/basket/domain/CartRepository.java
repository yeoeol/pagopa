package com.commerce.pagopa.basket.domain;

import java.util.Optional;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.CART_NOT_FOUND;

public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findById(Long id);

    void deleteById(Long id);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdWithItems(Long userId);

    default Cart findByUserIdOrThrow(Long userId) {
        return findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CART_NOT_FOUND));
    }

    default Cart findByUserIdWithItemsOrThrow(Long userId) {
        return findByUserIdWithItems(userId)
                .orElseThrow(() -> new BusinessException(CART_NOT_FOUND));
    }
}
