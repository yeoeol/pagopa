package com.commerce.pagopa.domain.order.service;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.cart.repository.CartRepository;
import com.commerce.pagopa.domain.order.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.domain.order.dto.request.OrderProductRequestDto;
import com.commerce.pagopa.domain.order.dto.request.OrderRequestDto;
import com.commerce.pagopa.domain.order.dto.request.OrderSearch;
import com.commerce.pagopa.domain.order.dto.response.OrderResponseDto;
import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    @Transactional
    public OrderResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Order order = Order.init(getOrderNumber(), requestDto.paymentMethod(), user);

        for (OrderProductRequestDto orderProductRequestDto : requestDto.products()) {
            Product product = productRepository.findById(orderProductRequestDto.productId())
                    .orElseThrow(ProductNotFoundException::new);

            int updatedRows = productRepository.decreaseStock(product.getId(), orderProductRequestDto.quantity());
            if (updatedRows == 0) {
                throw new ProductOutOfStockException();
            }

            OrderProduct orderProduct = OrderProduct.create(
                    orderProductRequestDto.quantity(),
                    product.getPrice(),
                    product
            );
            order.addOrderProduct(orderProduct);
        }

        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        order.cancel();

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            int updatedRows = productRepository.increaseStock(
                    orderProduct.getProduct().getId(),
                    orderProduct.getQuantity()
            );
            if (updatedRows == 0) {
                throw new ProductNotFoundException();
            }
        }
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        return OrderResponseDto.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> findAll(Long userId, OrderSearch orderSearch) {
        return orderRepository.findByUserIdAndStatus(userId, orderSearch.status()).stream()
                .map(OrderResponseDto::from)
                .toList();
    }

    @Transactional
    public OrderResponseDto orderFromCart(Long userId, OrderRequestDto requestDto) {
        List<Cart> carts = cartRepository.findAllByIdInAndUserId(requestDto.cartIds(), userId);
        if (carts.isEmpty()) {
            throw new CartNotFoundException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Order order = Order.init(getOrderNumber(), requestDto.paymentMethod(), user);

        for (Cart cart : carts) {
            Product product = cart.getProduct();

            int updatedRows = productRepository.decreaseStock(product.getId(), cart.getQuantity());
            if (updatedRows == 0) {
                throw new ProductOutOfStockException();
            }

            OrderProduct orderProduct = OrderProduct.create(
                    cart.getQuantity(),
                    product.getPrice(),
                    product
            );
            order.addOrderProduct(orderProduct);
        }

        // 주문 완료 후 장바구니 항목 삭제
        cartRepository.deleteAllById(requestDto.cartIds());
        return OrderResponseDto.from(orderRepository.save(order));
    }

    private static String getOrderNumber() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
