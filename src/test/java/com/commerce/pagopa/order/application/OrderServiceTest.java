package com.commerce.pagopa.order.application;

import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.SellerOrderNotFoundException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.support.fixture.ProductFixture;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartRepository cartRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        OrderStockRestoreService orderStockRestoreService = new OrderStockRestoreService(productRepository);
        orderService = new OrderService(
                orderRepository,
                userRepository,
                productRepository,
                cartRepository,
                orderStockRestoreService
        );
    }

    @Test
    void cancelSellerOrder_cancelsSellerOrderAndRestoresStock() {
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder sellerOrder = order.getSellerOrders().getFirst();

        when(orderRepository.findByIdForUpdateOrThrow(1L)).thenReturn(order);

        orderService.cancelSellerOrder(1L, sellerOrder.getId(), new OrderCancelRequestDto("buyer cancel"));

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);
        verify(productRepository).increaseStock(101L, 1);
    }

    @Test
    void cancelSellerOrder_throwsWhenSellerOrderNotInThisOrder() {
        Order order = newPaidOrderWithTwoSellers();

        when(orderRepository.findByIdForUpdateOrThrow(1L)).thenReturn(order);

        assertThatThrownBy(() -> orderService.cancelSellerOrder(1L, 999L, new OrderCancelRequestDto("wrong id")))
                .isInstanceOf(SellerOrderNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_NOT_FOUND);

        verify(productRepository, never()).increaseStock(101L, 1);
        verify(productRepository, never()).increaseStock(102L, 1);
    }

    @Test
    void cancelSellerOrder_throwsAfterDeliveringWithoutRestoringStock() {
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder sellerOrder = order.getSellerOrders().getFirst();
        sellerOrder.deliver();

        when(orderRepository.findByIdForUpdateOrThrow(1L)).thenReturn(order);

        assertThatThrownBy(() -> orderService.cancelSellerOrder(1L, sellerOrder.getId(), new OrderCancelRequestDto("late cancel")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_CANNOT_CANCEL);

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.DELIVERING);
        verify(productRepository, never()).increaseStock(101L, 1);
    }

    @Test
    void cancelOrder_cancelsRemainingSellerOrdersAndRestoresOnlyActiveStock() {
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder alreadyCancelled = order.getSellerOrders().get(0);
        SellerOrder active = order.getSellerOrders().get(1);
        alreadyCancelled.cancelByBuyer();

        when(orderRepository.findByIdForUpdateOrThrow(1L)).thenReturn(order);

        orderService.cancelOrder(1L, new OrderCancelRequestDto("cancel remaining"));

        assertThat(alreadyCancelled.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(active.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productRepository, never()).increaseStock(101L, 1);
        verify(productRepository).increaseStock(102L, 1);
    }

    @Test
    void cancelUnpaidOrder_cancelsPendingOrderAndRestoresStock() {
        Order order = newPendingOrder();
        SellerOrder sellerOrder = order.getSellerOrders().getFirst();

        when(orderRepository.findByIdForUpdateOrThrow(1L)).thenReturn(order);

        orderService.cancelUnpaidOrder(1L);

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productRepository).increaseStock(101L, 1);
    }

    private Order newPaidOrderWithTwoSellers() {
        Order order = newOrder();
        ReflectionTestUtils.setField(order, "id", 1L);

        SellerOrder sellerOrder1 = newSellerOrder("seller-1", "ORD-1-1", 101L, new BigDecimal("4000"));
        ReflectionTestUtils.setField(sellerOrder1, "id", 11L);
        sellerOrder1.pay();
        order.addSellerOrder(sellerOrder1);

        SellerOrder sellerOrder2 = newSellerOrder("seller-2", "ORD-1-2", 102L, new BigDecimal("3000"));
        ReflectionTestUtils.setField(sellerOrder2, "id", 12L);
        sellerOrder2.pay();
        order.addSellerOrder(sellerOrder2);

        return order;
    }

    private Order newPendingOrder() {
        Order order = newOrder();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.addSellerOrder(newSellerOrder("seller-1", "ORD-1-1", 101L, new BigDecimal("4000")));
        return order;
    }

    private Order newOrder() {
        Address address = new Address("12345", "address", "detail");
        Delivery delivery = Delivery.create(address, "recipient", "01012345678", null);
        User buyer = UserFixture.aBuyer("buyer");
        return Order.init("ORD-1", PaymentMethod.CARD, buyer, delivery);
    }

    private SellerOrder newSellerOrder(String sellerProviderId, String sellerOrderNumber, Long productId, BigDecimal price) {
        User seller = UserFixture.aSeller(sellerProviderId);
        Product product = ProductFixture.aProduct(null, seller, price);
        ReflectionTestUtils.setField(product, "id", productId);

        SellerOrder sellerOrder = SellerOrder.create(seller, sellerOrderNumber);
        sellerOrder.addOrderProduct(OrderProduct.create(1, price, product));
        return sellerOrder;
    }
}
