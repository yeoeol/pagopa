package com.commerce.pagopa.order.application;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.application.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderProductRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
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

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    /**
     * 바로 주문을 생성합니다.
     *
     * [처리 순서]
     * 1단계. 동일 상품 수량 합산 (같은 productId가 여러 번 오면 수량을 합칩니다.)
     * 2단계. 모든 상품 검증 (존재 여부 / 판매 여부 / 재고 부족 여부)
     * 3단계. 검증 통과 후 OrderProduct 목록 생성 및 총액 계산
     * 4단계. 재고 차감
     * 5단계. 주문 저장 및 응답 반환
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        // 1단계: 동일 상품 수량 합산
        Map<Long, Integer> totalQuantityByProductId = new HashMap<>();

        for (OrderProductRequestDto orderProduct : requestDto.products()) {
            totalQuantityByProductId.merge(orderProduct.productId(), orderProduct.quantity(), Integer::sum);
        }

        // 2단계: 모든 상품 검증 (존재 여부 / 판매 여부 / 재고 부족 여부)
        Map<Long, Product> productMap = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : totalQuantityByProductId.entrySet()) {
            Long productId = entry.getKey();
            int totalQuantity = entry.getValue();

            Product product = productRepository.findByIdOrThrow(productId);
            if (!product.isActive()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
            }

            if (product.getStock() < totalQuantity) {
                throw new BusinessException(
                        ErrorCode.PRODUCT_OUT_OF_STOCK,
                        "재고가 부족합니다. productId=%d, 현재 재고=%d, 요청 수량=%d"
                                .formatted(productId, product.getStock(), totalQuantity)
                );
            }

            productMap.put(productId, product);     // 검증을 통과한 메뉴들 보관
        }

        // 3단계: OrderProduct 목록 생성 및 총액 계산
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
            orderProduct.addOrder(order);
        }

        // 4단계: 재고 차감
        for (Map.Entry<Long, Integer> entry : totalQuantityByProductId.entrySet()) {
            Product product = productMap.get(entry.getKey());
            int orderedQuantity = entry.getValue();

            product.changeStock(product.getStock() - orderedQuantity);
        }

        // 5단계: 주문 저장 및 응답 반환
        return OrderResponseDto.from(orderRepository.save(order));
    }

    /**
     * 장바구니 목록 주문을 생성합니다.
     *
     * [처리 순서]
     * 1단계. 장바구니 목록 조회
     * 2단계. order() 메서드에 보내기 위한 재료 만들기 (List<OrderProductRequestDto>, OrderCreateRequestDto)
     * 3단계. order() 호출
     * 4단계. 장바구니 목록 삭제
     * 5단계. 응답 반환
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto orderFromCart(Long userId, CartOrderRequestDto requestDto) {
        // 1단계: 장바구니 목록 조회
        List<Cart> carts = cartRepository.findAllByIdInAndUserId(requestDto.cartIds(), userId);
        if (carts.isEmpty()) {
            throw new CartNotFoundException();
        }

        // 2단계: order() 메서드에 보내기 위한 재료 만들기
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

        // 3단계: order() 호출
        OrderResponseDto response = order(userId, orderCreateRequestDto);

        // 4단계: 장바구니 목록 삭제
        cartRepository.deleteAllByIdIn(carts.stream().map(Cart::getId).toList());

        // 5단계: 응답 반환
        return response;
    }

    /**
     * 주문 취소 로직
     *
     * [처리 순서]
     * 1단계. 주문 존재 여부 확인
     * 2단계. 취소 가능한 상태인지 확인
     * 3단계. 주문 항목 수량만큼 재고 복구
     * 4단계. 주문 상태를 CANCELLED로 변경 및 저장
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        // 1단계: 주문 존재 여부 확인
        Order order = orderRepository.findByIdOrThrow(orderId);

        // 2단계: 취소 가능한 상태인지 확인
        if (order.getStatus() != OrderStatus.ORDERED
                && order.getStatus() != OrderStatus.PAID
        ) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }

        // 3단계: 주문 항목 수량만큼 재고 복구
        for (OrderProduct op : order.getOrderProducts()) {
            Product product = productRepository.findByIdOrThrow(op.getProductId());
            product.changeStock(product.getStock() + op.getQuantity());
        }

        // 4단계: 주문 상태를 CANCELLED로 변경 및 저장
        order.changeStatus(OrderStatus.CANCELLED);
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
