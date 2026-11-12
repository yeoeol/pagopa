package com.commerce.pagopa.basket.domain;

import java.util.Optional;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.CART_ITEM_NOT_FOUND;
import static com.commerce.pagopa.global.response.ErrorCode.CART_NOT_FOUND;

public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findById(Long id);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdWithItems(Long userId);

    Optional<Cart> findByCartItemIdForUpdate(Long cartItemId);

    default Cart findByUserIdOrThrow(Long userId) {
        return findByUserId(userId).orElseThrow(() -> new BusinessException(CART_NOT_FOUND));
    }

    default Cart findByCartItemIdForUpdateOrThrow(Long cartItemId) {
        return findByCartItemIdForUpdate(cartItemId)
                .orElseThrow(() -> new BusinessException(CART_ITEM_NOT_FOUND));
    }
}
