package com.commerce.pagopa.cartitem.application.reseponse;

import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;

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
