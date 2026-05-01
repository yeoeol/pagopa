package com.commerce.pagopa.cart.application.dto.response;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

public record CartResponseDto(
        Long cartId,
        Integer quantity,
        UserResponseDto user,
        ProductResponseDto product
) {
    public static CartResponseDto from(Cart cart) {
        return new CartResponseDto(
                cart.getId(),
                cart.getQuantity(),
                UserResponseDto.from(cart.getUser()),
                ProductResponseDto.from(cart.getProduct())
        );
    }
}
