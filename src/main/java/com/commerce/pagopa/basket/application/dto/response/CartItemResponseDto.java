package com.commerce.pagopa.basket.application.dto.response;

import com.commerce.pagopa.basket.domain.CartItem;

public record CartItemResponseDto(
        Long cartId,
        Long productId,
        Integer cartQuantity
) {
    public static CartItemResponseDto from(CartItem cartItem) {
        return new CartItemResponseDto(
                cartItem.getCart().getId(),
                cartItem.getProductId(),
                cartItem.getCartQuantity()
        );
    }
}
