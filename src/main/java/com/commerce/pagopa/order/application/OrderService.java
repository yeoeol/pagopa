package com.commerce.pagopa.order.application;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.global.exception.CartNotFoundException;
import com.commerce.pagopa.order.application.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderProductRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import io.micrometer.core.annotation.Counted;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    /**
     * 바로 주문을 생성합니다.
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        // 동일 상품 수량 합산
        Map<Long, Integer> totalQuantityByProductId = new HashMap<>();

        for (OrderProductRequestDto orderProduct : requestDto.products()) {
            totalQuantityByProductId.merge(
                    orderProduct.productId(),
                    orderProduct.quantity(),
                    Integer::sum
            );
        }

        // 데드락 방지
        List<Long> productIds = totalQuantityByProductId.keySet()
                .stream()
                .sorted()
                .toList();

        // 모든 상품 검증 (존재 여부 / 판매 여부 / 재고 부족 여부)
        Map<Long, Product> productMap = new HashMap<>();

        // 존재 여부 검증 및 row 락 걸기
        for (Long productId : productIds) {
            Product product = productRepository.findByIdForUpdateOrThrow(productId);
            productMap.put(productId, product);
        }

        // OrderProduct 목록 생성 및 총액 계산
        User user = userRepository.findByIdOrThrow(userId);
        Order order = Order.init(
                user,
                requestDto.delivery().toDelivery()
        );

        for (OrderProductRequestDto op : requestDto.products()) {
            Product product = productMap.get(op.productId());

            OrderProduct orderProduct = OrderProduct.create(
                    product.getId(),
                    product.getName(),
                    op.quantity(),
                    product.getPrice()
            );
            order.addOrderProduct(orderProduct);
        }

        // 재고 차감
        for (Long productId : productIds) {
            Product product = productMap.get(productId);
            int orderedQuantity = totalQuantityByProductId.get(productId);
            product.decreaseStock(orderedQuantity);
        }

        return OrderResponseDto.from(orderRepository.save(order));
    }

    /**
     * 장바구니 목록 주문을 생성합니다.
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto orderFromCart(Long userId, CartOrderRequestDto requestDto) {
        // 장바구니 목록 조회
        List<Cart> carts = cartRepository.findAllByIdInAndUserId(requestDto.cartIds(), userId);
        if (carts.isEmpty()) {
            throw new CartNotFoundException();
        }

        // order() 메서드에 보내기 위한 재료 만들기
        List<OrderProductRequestDto> orderProductRequestDtos = new ArrayList<>();
        for (Cart cart : carts) {
            OrderProductRequestDto dto = new OrderProductRequestDto(
                    cart.getProduct().getId(),
                    cart.getQuantity()
            );
            orderProductRequestDtos.add(dto);
        }

        OrderCreateRequestDto orderCreateRequestDto = new OrderCreateRequestDto(
                requestDto.delivery(),
                orderProductRequestDtos
        );

        OrderResponseDto response = order(userId, orderCreateRequestDto);

        // 장바구니 목록 삭제
        cartRepository.deleteAllByIdIn(carts.stream().map(Cart::getId).toList());

        return response;
    }

    /**
     * 주문을 취소합니다.
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        // 주문 존재 여부 확인
        Order order = orderRepository.findByIdForUpdateOrThrow(orderId);
        order.cancel();

        // 데드락 방지
        List<Long> productIds = order.getOrderProducts().stream()
                .map(OrderProduct::getProductId)
                .distinct()
                .sorted()
                .toList();

        Map<Long, Product> productMap = new HashMap<>();

        for (Long productId : productIds) {
            Product product = productRepository.findByIdForUpdateOrThrow(productId);
            productMap.put(productId, product);
        }

        // 주문 항목 수량만큼 재고 복구
        for (OrderProduct op : order.getOrderProducts()) {
            Product product = productMap.get(op.getProductId());
            product.restoreStock(op.getQuantity());
        }

        return OrderResponseDto.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        return OrderResponseDto.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> findAll(Long userId, OrderSearch orderSearch, Pageable pageable) {
        OrderSearch search = orderSearch == null ? new OrderSearch(null, null) : orderSearch;
        LocalDateTime now = LocalDateTime.now();

        Page<Order> pageOrder = orderRepository.findAllByPeriod(
                userId,
                search.status(),
                search.start(now),
                search.end(now),
                pageable
        );
        return pageOrder.map(OrderResponseDto::from);
    }
}
