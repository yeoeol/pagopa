package com.commerce.pagopa.basket.application;

import com.commerce.pagopa.basket.application.dto.request.CartItemAddRequestDto;
import com.commerce.pagopa.basket.application.dto.response.CartItemResponseDto;
import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.basket.domain.CartItemRepository;
import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductSummary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final ProductApi productApi;

    @Transactional
    public CartItemResponseDto addCartItem(Long userId, CartItemAddRequestDto requestDto) {
        Cart cart = cartService.getOrCreate(userId);

        ProductSummary product = productApi.get(requestDto.productId());

        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart, product.productId())
                .map(existing -> {
                    existing.addQuantity(requestDto.quantity());
                    return existing;
                })
                .orElseGet(() -> cartItemRepository.save(
                        CartItem.create(cart, product.productId(), requestDto.quantity()))
                );

        return CartItemResponseDto.from(cartItem);
    }

    @Transactional
    public CartItemResponseDto incrementQuantity(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdForUpdateOrThrow(cartItemId);
        cartItem.addQuantity(1);
        return CartItemResponseDto.from(cartItem);
    }

    @Transactional
    public CartItemResponseDto decrementQuantity(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdForUpdateOrThrow(cartItemId);
        cartItem.reduceQuantity(1);

        if (cartItem.getCartQuantity() == 0) {
            delete(cartItem.getId());
            return null;
        }
        return CartItemResponseDto.from(cartItem);
    }

    @Transactional
    public void delete(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}
