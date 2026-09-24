package com.commerce.pagopa.basket.application;

import com.commerce.pagopa.basket.api.CartItemApi;
import com.commerce.pagopa.basket.api.CartItemSummary;
import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.basket.domain.CartItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemApiService implements CartItemApi {

	private final CartItemRepository cartItemRepository;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CartItemSummary> findAllByIdInAndUserIdForUpdate(
			List<Long> cartItemIds,
			Long userId
	) {
		return cartItemRepository.findAllByIdInAndUserIdForUpdate(cartItemIds, userId).stream()
				.map(this::toSummary)
				.toList();
	}

	@Override
	public void deleteAllByIdIn(List<Long> cartItemIds) {
		cartItemRepository.deleteAllByIdIn(cartItemIds);
	}

	private CartItemSummary toSummary(CartItem cartItem) {
		return new CartItemSummary(
				cartItem.getId(),
				cartItem.getProduct().getId(),
				cartItem.getCartQuantity()
		);
	}
}
