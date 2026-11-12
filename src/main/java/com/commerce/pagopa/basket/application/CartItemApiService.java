package com.commerce.pagopa.basket.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.basket.api.CartItemApi;
import com.commerce.pagopa.basket.api.CartItemSummary;
import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.basket.domain.CartRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Service
@RequiredArgsConstructor
public class CartItemApiService implements CartItemApi {

    private final CartRepository cartRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<CartItemSummary> findAllByIdInAndUserIdForUpdate(List<Long> cartItemIds, Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        return cart.getItemsByIds(cartItemIds)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAllByUserIdAndItemIdIn(
            Long userId,
            List<Long> cartItemIds
    ) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        cart.deleteAll(cartItemIds);
    }

    private CartItemSummary toSummary(CartItem cartItem) {
        return new CartItemSummary(
                cartItem.getId(),
                cartItem.getProductId(),
                cartItem.getCartQuantity()
        );
    }
}
