package com.commerce.pagopa.basket.presentation.security;

import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.basket.domain.CartItemRepository;
import com.commerce.pagopa.global.validator.OwnerValidator;

import org.springframework.stereotype.Component;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Component("cartItemOwnerValidator")
@RequiredArgsConstructor
public class CartItemOwnerValidator extends OwnerValidator<CartItem, Long> {

    private final CartItemRepository cartItemRepository;

    @Override
    protected Optional<CartItem> findResource(Long cartItemId) {
        return cartItemRepository.findById(cartItemId);
    }

    @Override
    protected Long extractOwnerId(CartItem cartItem) {
        return cartItem.getCart().getUserId();
    }
}
