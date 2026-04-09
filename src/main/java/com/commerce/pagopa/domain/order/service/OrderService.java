package com.commerce.pagopa.domain.order.service;

import com.commerce.pagopa.domain.order.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.domain.order.dto.request.OrderProductRequestDto;
import com.commerce.pagopa.domain.order.dto.request.OrderSearch;
import com.commerce.pagopa.domain.order.dto.response.OrderResponseDto;
import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.OrderNotFoundException;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
import com.commerce.pagopa.global.exception.ProductOutOfStockException;
import com.commerce.pagopa.global.exception.UserNotFoundException;
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

    private static String getOrderNumber() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
