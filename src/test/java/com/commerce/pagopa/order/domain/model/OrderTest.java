package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.SellerOrderNotFoundException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.support.fixture.OrderFixture;
import com.commerce.pagopa.support.fixture.OrderProductFixture;
import com.commerce.pagopa.support.fixture.ProductFixture;
import com.commerce.pagopa.support.fixture.SellerOrderFixture;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final BigDecimal PRICE = new BigDecimal("10000");

    @Test
    void getStatus_returnsPaidWhenAllSellerOrdersAreReady() {
        Order order = newOrder();
        order.addSellerOrder(newReadySellerOrder("seller-1", "order-1-1"));
        order.addSellerOrder(newReadySellerOrder("seller-2", "order-1-2"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void getStatus_returnsDeliveringWhenSellerOrdersAreMixed() {
        Order order = newOrder();
        SellerOrder completedSellerOrder = newReadySellerOrder("seller-1", "order-1-1");
        completedSellerOrder.deliver();
        completedSellerOrder.complete();
        order.addSellerOrder(completedSellerOrder);
        order.addSellerOrder(newReadySellerOrder("seller-2", "order-1-2"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    void cancel_validatesBeforeMutatingAnySellerOrder() {
        Order order = newOrder();

        SellerOrder readySellerOrder = newReadySellerOrder("seller-1", "order-1-1");
        SellerOrder deliveringSellerOrder = newReadySellerOrder("seller-2", "order-1-2");
        deliveringSellerOrder.deliver();

        order.addSellerOrder(readySellerOrder);
        order.addSellerOrder(deliveringSellerOrder);

        assertThatThrownBy(order::cancel)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_CANNOT_CANCEL);

        assertThat(readySellerOrder.getStatus()).isEqualTo(SellerOrderStatus.READY);
        assertThat(deliveringSellerOrder.getStatus()).isEqualTo(SellerOrderStatus.DELIVERING);
    }

    @Test
    void cancel_doesNotRestoreStockTwiceForAlreadyCancelledSellerOrder() {
        // 시나리오: SellerOrder#1은 사전에 부분 취소되어 재고 복원이 이미 1회 일어남.
        // 그 후 전체 Order.cancel() 호출 시 SellerOrder#1은 건드리지 않고 SellerOrder#2만 취소되어야 함.

        User seller1 = UserFixture.aSeller("seller-1");
        User seller2 = UserFixture.aSeller("seller-2");
        Product product1 = ProductFixture.aProduct(null, seller1, PRICE);
        Product product2 = ProductFixture.aProduct(null, seller2, PRICE);

        Order order = newOrder();

        SellerOrder so1 = SellerOrderFixture.aSellerOrder(seller1, "order-1-1");
        product1.decreaseStock(1);   // 주문 placement 시 차감
        so1.addOrderProduct(OrderProductFixture.anOrderProduct(product1));
        so1.pay();
        order.addSellerOrder(so1);

        SellerOrder so2 = SellerOrderFixture.aSellerOrder(seller2, "order-1-2");
        product2.decreaseStock(1);
        so2.addOrderProduct(OrderProductFixture.anOrderProduct(product2));
        so2.pay();
        order.addSellerOrder(so2);

        // SellerOrder#1만 사전에 취소 → product1 재고 9 → 10으로 복원
        so1.cancel();
        assertThat(product1.getStock()).isEqualTo(10);
        assertThat(product2.getStock()).isEqualTo(9);

        // 전체 주문 취소 — so1은 스킵되고 so2만 취소되어야 함
        order.cancel();

        assertThat(product1.getStock()).isEqualTo(10);  // 중복 복원되지 않음
        assertThat(product2.getStock()).isEqualTo(10);
    }

    @Test
    void findSellerOrder_returnsMatchingSellerOrder() {
        Order order = newOrder();
        SellerOrder so1 = newReadySellerOrder("seller-1", "order-1-1");
        SellerOrder so2 = newReadySellerOrder("seller-2", "order-1-2");
        ReflectionTestUtils.setField(so1, "id", 1L);
        ReflectionTestUtils.setField(so2, "id", 2L);
        order.addSellerOrder(so1);
        order.addSellerOrder(so2);

        SellerOrder found = order.findSellerOrder(2L);

        assertThat(found).isSameAs(so2);
    }

    @Test
    void findSellerOrder_throwsWhenSellerOrderIdNotInThisOrder() {
        Order order = newOrder();
        SellerOrder so1 = newReadySellerOrder("seller-1", "order-1-1");
        ReflectionTestUtils.setField(so1, "id", 1L);
        order.addSellerOrder(so1);

        assertThatThrownBy(() -> order.findSellerOrder(999L))
                .isInstanceOf(SellerOrderNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_NOT_FOUND);
    }

    @Test
    void calculateActiveAmount_excludesCancelledSellerOrders() {
        Order order = newOrder();
        SellerOrder so1 = newReadySellerOrder("seller-1", "order-1-1");
        SellerOrder so2 = newReadySellerOrder("seller-2", "order-1-2");
        order.addSellerOrder(so1);
        order.addSellerOrder(so2);

        so1.cancelByBuyer();

        assertThat(order.calculateActiveAmount()).isEqualByComparingTo(so2.getSellerTotalAmount());
    }

    @Test
    void calculateActiveAmount_returnsZeroWhenAllCancelled() {
        Order order = newOrder();
        SellerOrder so1 = newReadySellerOrder("seller-1", "order-1-1");
        SellerOrder so2 = newReadySellerOrder("seller-2", "order-1-2");
        order.addSellerOrder(so1);
        order.addSellerOrder(so2);

        so1.cancelByBuyer();
        so2.cancelByBuyer();

        assertThat(order.calculateActiveAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Order newOrder() {
        return OrderFixture.anOrder("order-1", UserFixture.aBuyer("buyer"));
    }

    private SellerOrder newReadySellerOrder(String providerId, String sellerOrderNumber) {
        User seller = UserFixture.aSeller(providerId);
        Product product = ProductFixture.aProduct(null, seller, PRICE);
        SellerOrder sellerOrder = SellerOrderFixture.aSellerOrder(seller, sellerOrderNumber);
        sellerOrder.addOrderProduct(OrderProductFixture.anOrderProduct(product));
        sellerOrder.pay();
        return sellerOrder;
    }
}
