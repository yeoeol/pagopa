package com.commerce.pagopa.ordering.application;

import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.basket.domain.CartItemRepository;
import com.commerce.pagopa.catalog.api.*;
import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRepository;
import com.commerce.pagopa.ordering.application.dto.request.*;
import com.commerce.pagopa.ordering.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.ordering.application.dto.response.OrderStockResponseDto;
import com.commerce.pagopa.ordering.domain.delivery.Delivery;
import com.commerce.pagopa.ordering.domain.delivery.DeliveryRepository;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.ordering.domain.order.OrderRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final CartItemRepository cartItemRepository;
    private final ProductStockApi productStockApi;
    private final ProductApi productApi;

    /**
     * 바로 주문을 생성합니다.
     */
    @Counted("my.order")
    @Transactional
    public OrderStockResponseDto order(Long userId, OrderCreateRequestDto requestDto) {
        List<ProductStockRequest> stockRequests =
                requestDto.products().stream()
                        .map(item -> new ProductStockRequest(
                                item.productId(),
                                item.quantity()
                        ))
                        .toList();

        List<ProductStockResult> products = productStockApi.decreaseStocks(stockRequests);
        Map<Long, ProductSummary> summary = productApi.findAllByIds(
                products.stream()
                        .map(ProductStockResult::productId).toList()
        );

        // OrderItem 목록 생성 및 총액 계산
        User user = userRepository.findByIdOrThrow(userId);
        Order order = Order.init(user);

        for (ProductStockResult result : products) {
            OrderItem orderItem = OrderItem.create(
                    result.productName(),
                    result.unitPrice(),
                    result.requestedQuantity(),
                    order,
                    result.productId()
            );
            order.addOrderItem(orderItem);
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

        return OrderStockResponseDto.from(savedOrder, summary);
    }

    /**
     * 장바구니 목록 주문을 생성합니다.
     */
    @Counted("my.order")
    @Transactional
    public OrderStockResponseDto orderFromCart(Long userId, CartItemOrderRequestDto requestDto) {
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
        OrderStockResponseDto response = order(userId, orderCreateRequestDto);

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
    public OrderStockResponseDto cancelOrder(Long orderId) {
        // 주문 존재 여부 확인
        Order order = orderRepository.findByIdForUpdateOrThrow(orderId);
        order.cancel(LocalDateTime.now());

        List<ProductStockResult> products = productStockApi.restoreStocks(
                order.getOrderItems()
                        .stream()
                        .map(oi -> new ProductStockRequest(
                                oi.getProductId(), oi.getOrderQuantity()
                        ))
                        .toList()
        );
        Map<Long, ProductSummary> summary = productApi.findAllByIds(
                products.stream()
                        .map(ProductStockResult::productId).toList()
        );

        return OrderStockResponseDto.from(order, summary);
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
