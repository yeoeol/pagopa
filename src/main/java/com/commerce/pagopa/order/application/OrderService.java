package com.commerce.pagopa.order.application;

import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.cartitem.domain.repository.CartItemRepository;
import com.commerce.pagopa.delivery.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.delivery.domain.model.Delivery;
import com.commerce.pagopa.delivery.domain.repository.DeliveryRepository;
import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.order.application.dto.request.CartItemOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.orderitem.application.dto.request.OrderItemRequestDto;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import io.micrometer.core.annotation.Counted;

import static com.commerce.pagopa.global.response.ErrorCode.CART_ITEM_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * 바로 주문을 생성합니다.
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        // 동일 상품 수량 합산
        Map<Long, Integer> totalQuantityByProductId = new HashMap<>();

        for (OrderItemRequestDto orderItem : requestDto.products()) {
            totalQuantityByProductId.merge(
                    orderItem.productId(),
                    orderItem.quantity(),
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

        // OrderItem 목록 생성 및 총액 계산
        User user = userRepository.findByIdOrThrow(userId);
        Order order = Order.init(user);

        for (OrderItemRequestDto op : requestDto.products()) {
            Product product = productMap.get(op.productId());

            OrderItem orderItem = OrderItem.create(
                    product.getName(),
                    product.getPrice(),
                    op.quantity(),
                    order,
                    product
            );
            order.addOrderItem(orderItem);
        }

        // 재고 차감
        for (Long productId : productIds) {
            Product product = productMap.get(productId);
            int orderedQuantity = totalQuantityByProductId.get(productId);
            product.decreaseStock(orderedQuantity);
        }

        Order savedOrder = orderRepository.save(order);

        // 배송 정보 생성
        DeliveryRequestDto deliveryRequestDto = requestDto.delivery();
        Delivery delivery = Delivery.create(
                Address.create(
                        deliveryRequestDto.zipcode(),
                        deliveryRequestDto.address(),
                        deliveryRequestDto.detailAddress()
                ),
                deliveryRequestDto.requestMemo(),
                savedOrder
        );
        deliveryRepository.save(delivery);

        return OrderResponseDto.from(savedOrder);
    }

    /**
     * 장바구니 목록 주문을 생성합니다.
     */
    @Counted("my.order")
    @Transactional
    public OrderResponseDto orderFromCart(Long userId, CartItemOrderRequestDto requestDto) {
        // 선택된 장바구니 항목 조회
        List<CartItem> cartItems = cartItemRepository.findAllByIdInAndUserIdForUpdate(
                requestDto.cartItemIds(),
                userId
        );
        validateRequestedCartItems(requestDto.cartItemIds(), cartItems);

        OrderCreateRequestDto orderCreateRequestDto = getOrderCreateRequestDto(
                requestDto,
                cartItems
        );
        OrderResponseDto response = order(userId, orderCreateRequestDto);

        // 장바구니 목록 삭제
        cartItemRepository.deleteAllByIdIn(
                cartItems.stream()
                        .map(CartItem::getId)
                        .toList()
        );
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
        List<Long> productIds = order.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .distinct()
                .sorted()
                .toList();

        Map<Long, Product> productMap = new HashMap<>();

        for (Long productId : productIds) {
            Product product = productRepository.findByIdForUpdateOrThrow(productId);
            productMap.put(productId, product);
        }

        // 주문 항목 수량만큼 재고 복구
        for (OrderItem op : order.getOrderItems()) {
            Product product = productMap.get(op.getProduct().getId());
            product.restoreStock(op.getOrderQuantity());
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
        Instant now = Instant.now();

        Page<Order> pageOrder = orderRepository.findAllByPeriod(
                userId,
                search.status(),
                search.start(now),
                search.end(now),
                pageable
        );
        return pageOrder.map(OrderResponseDto::from);
    }

    private OrderCreateRequestDto getOrderCreateRequestDto(
            CartItemOrderRequestDto requestDto,
            List<CartItem> cartItems
    ) {
        if (cartItems.isEmpty()) {
            throw new BusinessException(CART_ITEM_NOT_FOUND);
        }

        // order() 메서드에 보내기 위한 재료 만들기
        List<OrderItemRequestDto> orderItemRequestDtos = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItemRequestDto dto = new OrderItemRequestDto(
                    cartItem.getProduct().getId(),
                    cartItem.getCartQuantity()
            );
            orderItemRequestDtos.add(dto);
        }

        return new OrderCreateRequestDto(
                requestDto.delivery(),
                orderItemRequestDtos
        );
    }

    private void validateRequestedCartItems(
            List<Long> requestedItemIds,
            List<CartItem> cartItems
    ) {
        Set<Long> requestedIds = new HashSet<>(requestedItemIds);
        Set<Long> foundIds = cartItems.stream()
                .map(CartItem::getId)
                .collect(Collectors.toSet());
        if (!requestedIds.equals(foundIds)) {
            throw new BusinessException(CART_ITEM_NOT_FOUND);
        }
    }
}
