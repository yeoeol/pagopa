package com.commerce.pagopa.domain.order.service;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.cart.repository.CartRepository;
import com.commerce.pagopa.domain.order.dto.request.*;
import com.commerce.pagopa.domain.order.dto.response.OrderResponseDto;
import com.commerce.pagopa.domain.order.entity.Address;
import com.commerce.pagopa.domain.order.entity.Delivery;
import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
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

    // 바로 주문
    @Transactional
    public OrderResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Order order = createOrderProcess(user, requestDto.paymentMethod(), requestDto.delivery());

        for (OrderProductRequestDto orderProductRequestDto : requestDto.products()) {
            Product product = productRepository.findById(orderProductRequestDto.productId())
                    .orElseThrow(ProductNotFoundException::new);
            processOrderProduct(order, product, orderProductRequestDto.quantity());
        }

        String orderName = getOrderName(order);
        order.assignOrderName(orderName);

        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 장바구니 목록 주문
    @Transactional
    public OrderResponseDto orderFromCart(Long userId, CartOrderRequestDto requestDto) {
        List<Cart> carts = cartRepository.findAllByIdInAndUserId(requestDto.cartIds(), userId);
        if (carts.isEmpty()) {
            throw new CartNotFoundException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Order order = createOrderProcess(user, requestDto.paymentMethod(), requestDto.delivery());

        for (Cart cart : carts) {
            processOrderProduct(order, cart.getProduct(), cart.getQuantity());
        }

        String orderName = getOrderName(order);
        order.assignOrderName(orderName);

        // 주문 완료 후 장바구니 항목 삭제
        cartRepository.deleteAllById(requestDto.cartIds());
        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        order.markAsCancelled();

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

    private Order createOrderProcess(User user, PaymentMethod paymentMethod, DeliveryRequestDto deliveryDto) {
        Order order = Order.init(getOrderNumber(), paymentMethod, user);

        // 배송 정보 생성 및 연관관계 매핑
        Address address = new Address(
                deliveryDto.zipcode(),
                deliveryDto.address(),
                deliveryDto.detailAddress()
        );
        Delivery delivery = Delivery.create(
                address,
                deliveryDto.recipientName(),
                deliveryDto.recipientPhone(),
                deliveryDto.deliveryRequestMemo()
        );
        order.assignDelivery(delivery);

        return order;
    }

    private void processOrderProduct(Order order, Product product, int quantity) {
        int updatedRows = productRepository.decreaseStock(product.getId(), quantity);
        if (updatedRows == 0) {
            throw new ProductOutOfStockException();
        }

        OrderProduct orderProduct = OrderProduct.create(
                quantity,
                product.getPrice(),
                product
        );
        order.addOrderProduct(orderProduct);
    }

    private static String getOrderName(Order order) {
        return order.getOrderProducts().size() > 1
                ? "%s 외 %s건".formatted(
                order.getOrderProducts().getFirst().getProduct().getName(),
                order.getOrderProducts().size() - 1)
                : order.getOrderProducts().getFirst().getProduct().getName();
    }

    private static String getOrderNumber() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
