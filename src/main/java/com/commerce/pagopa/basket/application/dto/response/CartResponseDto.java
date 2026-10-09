package com.commerce.pagopa.basket.application.dto.response;

import java.util.Collections;
import java.util.List;

import com.commerce.pagopa.basket.domain.Cart;

public record CartResponseDto(
        Long cartId,
        Long userId,
        List<CartItemResponseDto> cartItems
) {
    public static CartResponseDto from(Cart cart) {
        return new CartResponseDto(
                cart.getId(),
                cart.getUserId(),
                cart.getCartItems()
                        .stream()
                        .map(CartItemResponseDto::from)
                        .toList()
        );
    }

    public static CartResponseDto empty(Long userId) {
        return new CartResponseDto(
                null,
                userId,
                Collections.emptyList()
        );
    }
}
