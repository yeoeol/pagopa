package com.commerce.pagopa.cart.presentation.security;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.infrastructure.persistence.CartJpaRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.validator.OwnerValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cartOwnerValidator")
@RequiredArgsConstructor
public class CartOwnerValidator extends OwnerValidator<Cart, Long> {

    private final CartJpaRepository cartRepository;

    @Override
    protected Optional<Cart> findResource(Long cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    protected Long extractOwnerId(Cart cart) {
        return Optional.ofNullable(cart.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
