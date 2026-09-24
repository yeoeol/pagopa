package com.commerce.pagopa.basket.application.dto.response;

import com.commerce.pagopa.basket.domain.Cart;

import java.util.Collections;
import java.util.List;

public record CartResponseDto(
		Long cartId,
		Long userId,
		List<CartItemResponseDto> cartItems
) {
	public static CartResponseDto from(Cart cart) {
		return new CartResponseDto(
				cart.getId(),
				cart.getUserId(),
				cart.getCartItems().stream()
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
