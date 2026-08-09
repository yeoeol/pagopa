package com.commerce.pagopa.cartitem.presentation.security;

import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.cartitem.domain.repository.CartItemRepository;
import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.user.domain.model.User;

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
        return Optional.ofNullable(cartItem.getCart().getUser())
                .map(User::getId)
                .orElse(null);
    }
}
