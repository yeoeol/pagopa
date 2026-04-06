package com.commerce.pagopa.domain.cart.service;

import com.commerce.pagopa.domain.cart.dto.request.CartAddRequestDto;
import com.commerce.pagopa.domain.cart.dto.request.CartUpdateRequestDto;
import com.commerce.pagopa.domain.cart.dto.response.CartResponseDto;
import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.cart.repository.CartRepository;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.CartNotFoundException;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
import com.commerce.pagopa.global.exception.UserNotFoundException;
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
    public CartResponseDto addCart(CartAddRequestDto requestDto) {
        User user = userRepository.findById(requestDto.userId())
                .orElseThrow(UserNotFoundException::new);
        Product product = productRepository.findById(requestDto.productId())
                .orElseThrow(ProductNotFoundException::new);

        Cart cart;
        if (cartRepository.findByUserAndProduct(user, product).isPresent()) {
            cart = cartRepository.findByUserAndProduct(user, product).get();
            if (requestDto.isAdd()) {
                cart.addQuantity();
            } else {
                cart.reduceQuantity();
            }
        } else {
            cart = Cart.create(requestDto.quantity(), user, product);
            cartRepository.save(cart);
        }
        
        return CartResponseDto.from(cart);
    }

    @Transactional(readOnly = true)
    public List<CartResponseDto> findUserCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<Cart> carts = cartRepository.findByUser(user);
        return carts.stream()
                .map(CartResponseDto::from)
                .toList();
    }

    @Transactional
    public CartResponseDto updateQuantity(Long cartId, boolean isAdd) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(CartNotFoundException::new);

        if (isAdd) {
            cart.addQuantity();
        } else {
            cart.reduceQuantity();
        }
        return CartResponseDto.from(cart);
    }

    @Transactional
    public void delete(Long cartId) {
        cartRepository.deleteById(cartId);
    }
}
