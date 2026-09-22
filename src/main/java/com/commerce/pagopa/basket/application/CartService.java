package com.commerce.pagopa.basket.application;

import com.commerce.pagopa.basket.application.dto.response.CartResponseDto;
import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.basket.domain.CartRepository;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponseDto findUserCart(Long userId) {
		return cartRepository.findByUserIdWithItems(userId)
                .map(CartResponseDto::from)
                .orElseGet(() -> {
                    User user = userRepository.findByIdOrThrow(userId);
                    return CartResponseDto.empty(user);
                });
    }

    @Transactional
    public void deleteAll(Long userId) {
        Cart cart = cartRepository.findByUserIdOrThrow(userId);
        cart.removeAllItems();
    }

    @Transactional
    public Cart getOrCreate(Long userId) {
        User user = userRepository.findByIdForUpdateOrThrow(userId);

		return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(user)));

	}
}
