package com.commerce.pagopa.basket.application.dto.response;

import com.commerce.pagopa.basket.domain.CartItem;

public record CartItemResponseDto(Long cartItemId, Long productId, Integer cartQuantity) {
    public static CartItemResponseDto from(CartItem cartItem) {
        return new CartItemResponseDto(cartItem.getId(), cartItem.getProductId(), cartItem.getCartQuantity());
    }
}
