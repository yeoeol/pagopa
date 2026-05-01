package com.commerce.pagopa.cart.application;

import com.commerce.pagopa.cart.application.dto.request.CartAddRequestDto;
import com.commerce.pagopa.cart.application.dto.response.CartResponseDto;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartResponseDto addCart(Long userId, CartAddRequestDto requestDto, boolean isAdd) {
        User user = userRepository.findByIdOrThrow(userId);
        Product product = productRepository.findByIdOrThrow(requestDto.productId());

        Cart cart;
        Optional<Cart> optionalCart = cartRepository.findByUserAndProduct(user, product);
        if (optionalCart.isPresent()) {
            cart = optionalCart.get();
            if (isAdd) { cart.addQuantity(); }
            else {
                cart.reduceQuantity();
                if (cart.getQuantity() == 0) {
                    delete(cart.getId());
                    return null;
                }
            }
        } else {
            cart = cartRepository.save(Cart.create(requestDto.quantity(), user, product));
        }

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
    public CartResponseDto updateQuantity(Long cartId, boolean isAdd) {
        Cart cart = cartRepository.findByIdWithFetchOrThrow(cartId);

        if (isAdd) {
            cart.addQuantity();
        } else {
            cart.reduceQuantity();
            if (cart.getQuantity() == 0) {
                delete(cart.getId());
                return null; // 삭제된 경우 null 반환
            }
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
