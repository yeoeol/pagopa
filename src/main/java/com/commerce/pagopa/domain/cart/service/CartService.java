package com.commerce.pagopa.domain.cart.service;

import com.commerce.pagopa.domain.cart.dto.request.CartAddRequestDto;
import com.commerce.pagopa.domain.cart.dto.response.CartResponseDto;
import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.cart.repository.CartRepository;
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
        User user = userRepository.getById(userId);
        Product product = productRepository.getById(requestDto.productId());

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
        User user = userRepository.getById(userId);

        List<Cart> carts = cartRepository.findByUser(user);
        return carts.stream()
                .map(CartResponseDto::from)
                .toList();
    }

    @Transactional
    public CartResponseDto updateQuantity(Long cartId, boolean isAdd) {
        Cart cart = cartRepository.getByIdWithFetch(cartId);

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
