package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

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

    private Order newOrder() {
        Address address = new Address("12345", "address", "detail");
        Delivery delivery = Delivery.create(address, "buyer", "01012345678", null);
        return Order.init(
                "order-1",
                PaymentMethod.CARD,
                newUser("buyer", Role.ROLE_USER),
                delivery
        );
    }

    private SellerOrder newReadySellerOrder(String providerId, String sellerOrderNumber) {
        User seller = newUser(providerId, Role.ROLE_SELLER);
        Product product = Product.create(
                "product-" + providerId,
                "description",
                new BigDecimal("10000"),
                null,
                10,
                null,
                seller
        );

        SellerOrder sellerOrder = SellerOrder.create(seller, sellerOrderNumber);
        sellerOrder.addOrderProduct(OrderProduct.create(1, new BigDecimal("10000"), product));
        sellerOrder.pay();
        return sellerOrder;
    }

    private User newUser(String providerId, Role role) {
        return User.create(
                providerId + "@example.com",
                providerId,
                null,
                Provider.GOOGLE,
                providerId,
                role
        );
    }
}
