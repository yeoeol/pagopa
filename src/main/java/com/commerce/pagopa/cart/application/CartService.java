package com.commerce.pagopa.cart.application;

import com.commerce.pagopa.cart.application.dto.response.CartResponseDto;
import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponseDto findUserCart(Long userId) {
        Optional<Cart> optionalCart = cartRepository.findByUserIdWithItems(userId);

        Cart cart = optionalCart.orElseGet(() -> {
            User user = userRepository.findByIdOrThrow(userId);
            return cartRepository.save(Cart.create(user));
        });
        return CartResponseDto.from(cart);
    }

    @Transactional
    public void deleteAll(Long userId) {
        Cart cart = cartRepository.findByUserIdOrThrow(userId);
        cart.removeAllItems();
    }
}
