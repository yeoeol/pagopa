package com.commerce.pagopa.order.application;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.order.application.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderProductRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.PaymentService;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.global.exception.*;

import io.micrometer.core.annotation.Counted;

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
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    // 바로 주문
    @Counted("my.order")
    @Transactional
    public OrderResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        User user = userRepository.findByIdOrThrow(userId);
        Order order = createOrderProcess(user, requestDto.paymentMethod(), requestDto.delivery());

        for (OrderProductRequestDto orderProductRequestDto : requestDto.products()) {
            Product product = productRepository.findByIdOrThrow(orderProductRequestDto.productId());
            processOrderProduct(order, product, orderProductRequestDto.quantity());
        }

        String orderName = getOrderName(order);
        order.assignOrderName(orderName);

        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 장바구니 목록 주문
    @Counted("my.order")
    @Transactional
    public OrderResponseDto orderFromCart(Long userId, CartOrderRequestDto requestDto) {
        List<Cart> carts = cartRepository.findAllByIdInAndUserId(requestDto.cartIds(), userId);
        if (carts.isEmpty()) {
            throw new CartNotFoundException();
        }

        User user = userRepository.findByIdOrThrow(userId);
        Order order = createOrderProcess(user, requestDto.paymentMethod(), requestDto.delivery());

        for (Cart cart : carts) {
            Product product = cart.getProduct();
            processOrderProduct(order, product, cart.getQuantity());
        }

        String orderName = getOrderName(order);
        order.assignOrderName(orderName);

        // 주문 완료 후 장바구니 항목 삭제
        List<Long> targetCartIds = carts.stream()
                    .map(Cart::getId)
                    .toList();
        cartRepository.deleteAllByIds(targetCartIds);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Counted("my.order")
    @Transactional
    public void cancelOrder(Long orderId, OrderCancelRequestDto requestDto) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        order.markAsCancelled();

        Payment payment = paymentRepository.getByOrderIdAndPaymentKeyOrThrow(orderId, requestDto.paymentKey());

        paymentService.cancelPayment(payment, requestDto.cancelReason());

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            Product product = orderProduct.getProduct();
            product.increaseStock(orderProduct.getQuantity());
        }
    }

    // 스케줄러 전용: Toss 미승인(paymentKey 없는) 미결제 주문 자동 취소
    @Transactional
    public void cancelUnpaidOrder(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        order.markAsCancelled();

        paymentService.cancelPaymentByOrder(order);

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            Product product = orderProduct.getProduct();
            product.increaseStock(orderProduct.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
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
        product.decreaseStock(quantity);

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
