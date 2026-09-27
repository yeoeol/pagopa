package com.commerce.pagopa.basket.application;

import com.commerce.pagopa.basket.application.dto.response.CartResponseDto;
import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.basket.domain.CartRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    @Transactional
    public CartResponseDto findUserCart(Long userId) {
		return cartRepository.findByUserIdWithItems(userId)
                .map(CartResponseDto::from)
                .orElseGet(() -> CartResponseDto.empty(userId));
    }

    @Transactional
    public void deleteAll(Long userId) {
        Cart cart = cartRepository.findByUserIdOrThrow(userId);
        cart.removeAllItems();
    }

    @Transactional
    public Cart getOrCreate(Long userId) {
		return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(userId)));
	}
}
