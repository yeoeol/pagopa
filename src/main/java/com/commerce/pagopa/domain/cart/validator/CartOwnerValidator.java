package com.commerce.pagopa.domain.cart.validator;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("cartOwnerValidator")
@RequiredArgsConstructor
public class CartOwnerValidator {

    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long cartId, Long userId) {
        if (cartId == null || userId == null) {
            return false;
        }
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart == null || cart.getUser() == null) {
            return false;
        }
        return cart.getUser().getId().equals(userId);
    }
}
