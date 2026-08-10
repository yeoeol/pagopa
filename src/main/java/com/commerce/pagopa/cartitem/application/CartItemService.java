package com.commerce.pagopa.cartitem.application;

import com.commerce.pagopa.cart.application.CartService;
import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cartitem.application.request.CartItemAddRequestDto;
import com.commerce.pagopa.cartitem.application.reseponse.CartItemResponseDto;
import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.cartitem.domain.repository.CartItemRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Transactional
    public CartItemResponseDto addCartItem(Long userId, CartItemAddRequestDto requestDto) {
        Cart cart = cartService.getOrCreate(userId);

        Product product = productRepository.findByIdOrThrow(requestDto.productId());

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .map(existing -> {
                    existing.addQuantity(requestDto.quantity());
                    return existing;
                })
                .orElseGet(() -> cartItemRepository.save(
                        CartItem.create(cart, product, requestDto.quantity()))
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
