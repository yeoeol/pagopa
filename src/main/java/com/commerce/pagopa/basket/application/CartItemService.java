package com.commerce.pagopa.basket.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.basket.application.dto.request.CartItemAddRequestDto;
import com.commerce.pagopa.basket.application.dto.response.CartItemResponseDto;
import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.basket.domain.CartRepository;
import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductSummary;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ProductApi productApi;

    @Transactional
    public CartItemResponseDto addCartItem(
            Long userId,
            CartItemAddRequestDto requestDto
    ) {
        Cart cart = cartService.getOrCreate(userId);

        ProductSummary product = productApi.get(requestDto.productId());

        CartItem item = cart.getItemByProductId(product.productId());
        if (item == null) {
            item = CartItem.create(
                    cart,
                    product.productId(),
                    requestDto.quantity()
            );
            cart.addItem(item);
        } else {
            cart.addItemQuantity(
                    item.getId(),
                    requestDto.quantity()
            );
        }

        return CartItemResponseDto.from(item);
    }

    @Transactional
    public CartItemResponseDto incrementQuantity(
            Long userId,
            Long productId
    ) {
        Cart cart = cartRepository.findByUserIdOrThrow(userId);
        cart.validateOwner(userId);

        CartItem item = cart.incrementItem(productId);
        return CartItemResponseDto.from(item);
    }

    @Transactional
    public CartItemResponseDto decrementQuantity(
            Long userId,
            Long productId
    ) {
        Cart cart = cartRepository.findByUserIdOrThrow(userId);

        CartItem item = cart.decrementItem(productId);
        return CartItemResponseDto.from(item);
    }

    @Transactional
    public void delete(
            Long userId,
            Long productId
    ) {
        Cart cart = cartRepository.findByUserIdOrThrow(userId);
        cart.deleteItem(productId);
    }
}
