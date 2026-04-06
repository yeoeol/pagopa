package com.commerce.pagopa.domain.cart.service;

import com.commerce.pagopa.domain.cart.dto.request.CartAddRequestDto;
import com.commerce.pagopa.domain.cart.dto.response.CartResponseDto;
import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.cart.repository.CartRepository;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
import com.commerce.pagopa.global.exception.UserNotFoundException;
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
    public CartResponseDto addCart(CartAddRequestDto requestDto) {
        User user = userRepository.findById(requestDto.userId())
                .orElseThrow(UserNotFoundException::new);
        Product product = productRepository.findById(requestDto.productId())
                .orElseThrow(ProductNotFoundException::new);

        Cart cart;
        if (cartRepository.findByUserAndProduct(user, product).isPresent()) {
            cart = cartRepository.findByUserAndProduct(user, product).get();
            cart.addQuantity(requestDto.quantity());
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
}
