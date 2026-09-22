package com.commerce.pagopa.basket.application.dto.response;

import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.catalog.application.dto.response.ProductResponseDto;

public record CartItemResponseDto(
        Long cartItemId,
        ProductResponseDto product,
        Integer cartQuantity
) {
    public static CartItemResponseDto from(CartItem cartItem) {
        return new CartItemResponseDto(
                cartItem.getId(),
                ProductResponseDto.from(cartItem.getProduct()),
                cartItem.getCartQuantity()
        );
    }
}
