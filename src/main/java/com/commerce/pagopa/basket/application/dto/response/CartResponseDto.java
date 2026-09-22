package com.commerce.pagopa.basket.application.dto.response;

import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.identity.application.dto.response.UserResponseDto;
import com.commerce.pagopa.identity.domain.User;

import java.util.Collections;
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

	public static CartResponseDto empty(User user) {
		return new CartResponseDto(
				null,
				UserResponseDto.from(user),
				Collections.emptyList()
		);
	}
}
