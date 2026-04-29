package com.commerce.pagopa.domain.cart.validator;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.cart.repository.CartRepository;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cartOwnerValidator")
@RequiredArgsConstructor
public class CartOwnerValidator extends OwnerValidator<Cart, Long> {

    private final CartRepository cartRepository;

    @Override
    protected Optional<Cart> findResource(Long cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    protected Long extractOwnerId(Cart cart) {
        return cart.getUser() == null ? null : cart.getUser().getId();
    }
}
