package com.commerce.pagopa.cart.application.dto.response;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cartitem.application.reseponse.CartItemResponseDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.util.List;

public record CartResponseDto(
		Long cartId,
		UserResponseDto user,
		List<CartItemResponseDto> cartItems
) {
	public static CartResponseDto from(Cart cart) {
		return new CartResponseDto(
				cart.getId(),
				UserResponseDto.from(cart.getUser()),
				cart.getCartItems().stream()
						.map(CartItemResponseDto::from)
						.toList()
		);
	}
}
