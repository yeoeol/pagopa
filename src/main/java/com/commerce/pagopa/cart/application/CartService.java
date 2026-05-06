package com.commerce.pagopa.cart.application;

import com.commerce.pagopa.cart.application.dto.request.CartAddRequestDto;
import com.commerce.pagopa.cart.application.dto.response.CartResponseDto;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartResponseDto addCart(Long userId, CartAddRequestDto requestDto) {
        User user = userRepository.findByIdOrThrow(userId);
        Product product = productRepository.findByIdOrThrow(requestDto.productId());

        Cart cart = cartRepository.findByUserAndProduct(user, product)
                .map(existing -> {
                    existing.addQuantity(requestDto.quantity());
                    return existing;
                })
                .orElseGet(() -> cartRepository.save(
                        Cart.create(requestDto.quantity(), user, product))
                );

        return CartResponseDto.from(cart);
    }

    @Transactional(readOnly = true)
    public List<CartResponseDto> findUserCart(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);

        List<Cart> carts = cartRepository.findByUser(user);
        return carts.stream()
                .map(CartResponseDto::from)
                .toList();
    }

    @Transactional
    public CartResponseDto incrementQuantity(Long cartId) {
        Cart cart = cartRepository.findByIdWithFetchOrThrow(cartId);
        cart.addQuantity(1);
        return CartResponseDto.from(cart);
    }

    @Transactional
    public CartResponseDto decrementQuantity(Long cartId) {
        Cart cart = cartRepository.findByIdWithFetchOrThrow(cartId);
        cart.reduceQuantity();
        if (cart.getQuantity() == 0) {
            delete(cart.getId());
            return null;
        }
        return CartResponseDto.from(cart);
    }

    @Transactional
    public void delete(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    @Transactional
    public void deleteAll(Long userId) {
        cartRepository.deleteAllByUserId(userId);
    }
}
