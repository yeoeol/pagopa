package com.commerce.pagopa.domain.cart.dto.response;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.user.dto.response.UserResponseDto;
import com.commerce.pagopa.domain.user.entity.User;

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

    public static CartResponseDto of(Cart cart, User user, Product product) {
        return new CartResponseDto(
                cart.getId(),
                cart.getQuantity(),
                UserResponseDto.from(user),
                ProductResponseDto.from(product)
        );
    }
}
