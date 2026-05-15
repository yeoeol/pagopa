package com.commerce.pagopa.order.application;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.order.application.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto.OrderProductRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.PaymentService;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.global.exception.*;

import io.micrometer.core.annotation.Counted;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
    private final OrderTransactionService orderTransactionService;

    // 바로 주문
    @Counted("my.order")
    @Transactional
    public OrderResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        User user = userRepository.findByIdOrThrow(userId);
        Order order = Order.init(
                generateOrderNumber(),
                requestDto.paymentMethod(),
                user,
                requestDto.delivery().toDelivery()
        );

        // 데드락 회피: 모든 트랜잭션이 productId 오름차순으로 행 락 획득
        List<OrderProductRequestDto> sorted = requestDto.products().stream()
                .sorted(Comparator.comparing(OrderProductRequestDto::productId))
                .toList();
        for (OrderProductRequestDto req : sorted) {
            int updated = productRepository.decreaseStock(req.productId(), req.quantity());
            if (updated == 0) {
                throw new ProductOutOfStockException();
            }
            Product product = productRepository.findByIdOrThrow(req.productId());
            addProductToOrder(order, product, req.quantity());
        }
        order.refresh();

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
        Order order = Order.init(
                generateOrderNumber(),
                requestDto.paymentMethod(),
                user,
                requestDto.delivery().toDelivery()
        );
        // 데드락 회피: productId 오름차순 정렬 후 행 락 획득
        carts.stream()
                .sorted(Comparator.comparing(cart -> cart.getProduct().getId()))
                .forEach(cart -> {
                    int updated = productRepository.decreaseStock(cart.getProduct().getId(), cart.getQuantity());
                    if (updated == 0) {
                        throw new ProductOutOfStockException();
                    }
                    addProductToOrder(order, cart.getProduct(), cart.getQuantity());
                });
        order.refresh();

        // 주문 완료 후 장바구니 항목 삭제
        cartRepository.deleteAllByIdIn(carts.stream().map(Cart::getId).toList());

        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Counted("my.order")
    public void cancelOrder(Long orderId, OrderCancelRequestDto requestDto) {
        OrderCancelCommand command = orderTransactionService.prepareCancelOrder(orderId);

        paymentService.cancelPayment(command.payment(), command.cancelAmount(), requestDto.cancelReason());
        orderTransactionService.markCancelOrderSuccess(command.orderId());
    }

    /**
     * 단일 SellerOrder 부분 취소
     */
    @Counted("my.order")
    public void cancelSellerOrder(Long orderId, Long sellerOrderId, OrderCancelRequestDto requestDto) {
        SellerOrderCancelCommand command = orderTransactionService.prepareCancelSellerOrder(orderId, sellerOrderId);

        paymentService.cancelPayment(command.payment(), command.cancelAmount(), requestDto.cancelReason());
        orderTransactionService.markCancelSellerOrderSuccess(command.orderId(), command.sellerOrderId());
    }

    // 스케줄러 전용: Toss 미승인(paymentKey 없는) 미결제 주문 자동 취소
    @Transactional
    public void cancelUnpaidOrder(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        order.cancel();

        paymentService.cancelPaymentByOrder(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        return OrderResponseDto.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> findAll(Long userId, OrderSearch orderSearch, Pageable pageable) {
        OrderStatus status = orderSearch == null ? null : orderSearch.status();
        return orderRepository.findAllByUserIdAndStatus(userId, status, pageable)
                .map(OrderResponseDto::from);
    }

    /**
     * 한 상품을 Order의 적절한 SellerOrder에 추가
     * 재고 차감은 ProductRepository.decreaseStock(id, qty) 단일 atomic UPDATE로 별도 처리
     */
    private void addProductToOrder(Order order, Product product, int quantity) {
        SellerOrder sellerOrder = order.findOrCreateSellerOrderFor(product.getSeller());
        sellerOrder.addOrderProduct(OrderProduct.create(quantity, product.getPrice(), product));
    }

    private static String generateOrderNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
