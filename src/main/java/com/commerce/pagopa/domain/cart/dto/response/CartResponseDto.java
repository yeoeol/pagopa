package com.commerce.pagopa.domain.cart.dto.response;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.user.dto.response.UserResponseDto;

public record CartResponseDto(
        Integer quantity,
        UserResponseDto user,
        ProductResponseDto product
) {
    public static CartResponseDto from(Cart cart) {
        return new CartResponseDto(
                cart.getQuantity(),
                UserResponseDto.from(cart.getUser()),
                ProductResponseDto.from(cart.getProduct())
        );
    }
}
