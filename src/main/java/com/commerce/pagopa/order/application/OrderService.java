package com.commerce.pagopa.order.application;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.order.application.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.model.SellerOrder;
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

import java.util.*;

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
        Order order = createOrder(user, requestDto.paymentMethod(), requestDto.delivery());

        // 상품 단위 입력 → 셀러 단위로 그룹핑
        List<OrderProductRequest> items = requestDto.products().stream()
                .map(req -> {
                    Product product = productRepository.findByIdOrThrow(req.productId());
                    return new OrderProductRequest(product, req.quantity());
                })
                .toList();

        attachSellerOrders(order, items);
        order.assignOrderName(getOrderName(order));

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
        Order order = createOrder(user, requestDto.paymentMethod(), requestDto.delivery());

        List<OrderProductRequest> items = carts.stream()
                .map(cart -> new OrderProductRequest(cart.getProduct(), cart.getQuantity()))
                .toList();

        attachSellerOrders(order, items);
        order.assignOrderName(getOrderName(order));

        // 주문 완료 후 장바구니 항목 삭제
        List<Long> targetCartIds = carts.stream()
                .map(Cart::getId)
                .toList();
        cartRepository.deleteAllByIdIn(targetCartIds);

        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Counted("my.order")
    @Transactional
    public void cancelOrder(Long orderId, OrderCancelRequestDto requestDto) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        order.cancel();

        Payment payment = paymentRepository.getByOrderIdAndPaymentKeyOrThrow(orderId, requestDto.paymentKey());

        paymentService.cancelPayment(payment, payment.getAmount(), requestDto.cancelReason());

        restoreStock(order);
    }

    // 스케줄러 전용: Toss 미승인(paymentKey 없는) 미결제 주문 자동 취소
    @Transactional
    public void cancelUnpaidOrder(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        order.cancel();

        paymentService.cancelPaymentByOrder(order);

        restoreStock(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        return OrderResponseDto.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> findAll(Long userId, OrderSearch orderSearch) {
        return orderRepository.findByUserId(userId).stream()
                .filter(order -> orderSearch.status() == null || order.getStatus() == orderSearch.status())
                .map(OrderResponseDto::from)
                .toList();
    }

    private Order createOrder(User user, PaymentMethod paymentMethod, DeliveryRequestDto deliveryDto) {
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
        return Order.init(getOrderNumber(), paymentMethod, user, delivery);
    }

    /**
     * 상품 목록을 셀러별로 그룹핑하여 SellerOrder를 생성하고 Order에 부착
     * 재고 차감도 함께 처리
     */
    private void attachSellerOrders(Order order, List<OrderProductRequest> items) {
        // 셀러별 그룹핑 (입력 순서 유지)
        Map<User, List<OrderProductRequest>> bySeller = new LinkedHashMap<>();
        for (OrderProductRequest item : items) {
            bySeller.computeIfAbsent(item.product().getSeller(), k -> new ArrayList<>()).add(item);
        }

        int seq = 1;
        for (Map.Entry<User, List<OrderProductRequest>> entry : bySeller.entrySet()) {
            User seller = entry.getKey();
            String sellerOrderNumber = "%s-%d".formatted(order.getOrderNumber(), seq++);
            SellerOrder sellerOrder = SellerOrder.create(seller, sellerOrderNumber);

            for (OrderProductRequest item : entry.getValue()) {
                Product product = item.product();
                product.decreaseStock(item.quantity());
                OrderProduct op = OrderProduct.create(item.quantity(), product.getPrice(), product);
                sellerOrder.addOrderProduct(op);
            }

            order.addSellerOrder(sellerOrder);
        }
    }

    private void restoreStock(Order order) {
        for (SellerOrder so : order.getSellerOrders()) {
            for (OrderProduct op : so.getOrderProducts()) {
                op.getProduct().increaseStock(op.getQuantity());
            }
        }
    }

    private static String getOrderName(Order order) {
        List<OrderProduct> all = order.getSellerOrders().stream()
                .flatMap(so -> so.getOrderProducts().stream())
                .toList();
        if (all.isEmpty()) {
            return "";
        }
        String firstName = all.getFirst().getProduct().getName();
        return all.size() > 1
                ? "%s 외 %d건".formatted(firstName, all.size() - 1)
                : firstName;
    }

    private static String getOrderNumber() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private record OrderProductRequest(Product product, int quantity) {}
}
