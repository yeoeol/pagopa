package com.commerce.pagopa.basket.domain;

import java.util.List;
import java.util.Optional;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

public interface CartItemRepository {
    CartItem save(CartItem cartItem);

    Optional<CartItem> findById(Long cartItemId);

    Optional<CartItem> findByIdForUpdate(Long cartItemId);

    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);

    List<CartItem> findAllByIdInAndUserIdForUpdate(List<Long> cartItemIds, Long userId);

    void deleteById(Long cartItemId);

    void deleteAllByIdIn(List<Long> cartItemIds);

    default CartItem findByIdOrThrow(Long cartItemId) {
        return findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    default CartItem findByIdForUpdateOrThrow(Long cartItemId) {
        return findByIdForUpdate(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }
}
