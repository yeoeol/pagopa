package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SellerOrderTest {

    @Test
    void create_initialStateIsPendingPayment() {
        SellerOrder sellerOrder = newPendingSellerOrder();

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.PENDING_PAYMENT);
        assertThat(sellerOrder.getSellerOrderNumber()).isEqualTo("ORD20260502-001-1");
        assertThat(sellerOrder.getSellerTotalAmount()).isEqualByComparingTo("10000");
    }

    @Test
    void pay_transitionsPendingPaymentToReady() {
        SellerOrder sellerOrder = newPendingSellerOrder();

        sellerOrder.pay();

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.READY);
    }

    @Test
    void deliver_transitionsReadyToDelivering() {
        SellerOrder sellerOrder = newReadySellerOrder();

        sellerOrder.deliver();

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.DELIVERING);
    }

    @Test
    void deliver_throwsWhenNotReady() {
        SellerOrder sellerOrder = newPendingSellerOrder();

        assertThatThrownBy(sellerOrder::deliver)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_CANNOT_DELIVER);
    }

    @Test
    void complete_transitionsDeliveringToCompleted() {
        SellerOrder sellerOrder = newReadySellerOrder();
        sellerOrder.deliver();

        sellerOrder.complete();

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.COMPLETED);
        assertThat(sellerOrder.isCompleted()).isTrue();
    }

    @Test
    void complete_throwsWhenNotDelivering() {
        SellerOrder sellerOrder = newReadySellerOrder();

        assertThatThrownBy(sellerOrder::complete)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_CANNOT_COMPLETE);
    }

    @Test
    void cancel_allowedInPendingPayment() {
        SellerOrder sellerOrder = newPendingSellerOrder();

        sellerOrder.cancel();

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(sellerOrder.isCancelled()).isTrue();
    }

    @Test
    void cancel_allowedInReady() {
        SellerOrder sellerOrder = newReadySellerOrder();

        sellerOrder.cancel();

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(sellerOrder.isCancelled()).isTrue();
    }

    @Test
    void cancel_throwsAfterDelivering() {
        SellerOrder sellerOrder = newReadySellerOrder();
        sellerOrder.deliver();

        assertThatThrownBy(sellerOrder::cancel)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_CANNOT_CANCEL);
    }

    @Test
    void cancelByBuyer_allowedInReady() {
        SellerOrder sellerOrder = newReadySellerOrder();

        sellerOrder.cancelByBuyer();

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(sellerOrder.isCancelled()).isTrue();
    }

    @Test
    void cancelByBuyer_restoresStockOnReady() {
        SellerOrder sellerOrder = newReadySellerOrder();
        Product product = sellerOrder.getOrderProducts().getFirst().getProduct();
        int stockBefore = product.getStock();
        product.decreaseStock(1); // 주문 placement 시 차감 흉내

        sellerOrder.cancelByBuyer();

        assertThat(product.getStock()).isEqualTo(stockBefore);
    }

    @Test
    void cancelByBuyer_throwsInPendingPayment() {
        SellerOrder sellerOrder = newPendingSellerOrder();

        assertThatThrownBy(sellerOrder::cancelByBuyer)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_CANNOT_CANCEL);
    }

    @Test
    void cancelByBuyer_throwsAfterDeliveringWithReturnGuidance() {
        SellerOrder sellerOrder = newReadySellerOrder();
        sellerOrder.deliver();

        assertThatThrownBy(sellerOrder::cancelByBuyer)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("발송 후에는 반품으로 처리하세요");
    }

    @Test
    void cancelByBuyer_throwsAfterCompletedWithReturnGuidance() {
        SellerOrder sellerOrder = newReadySellerOrder();
        sellerOrder.deliver();
        sellerOrder.complete();

        assertThatThrownBy(sellerOrder::cancelByBuyer)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("발송 후에는 반품으로 처리하세요");
    }

    private SellerOrder newPendingSellerOrder() {
        User seller = User.create(
                "seller@example.com",
                "seller",
                null,
                Provider.GOOGLE,
                "seller-provider-id",
                Role.ROLE_SELLER
        );
        Product product = Product.create(
                "product",
                "description",
                new BigDecimal("10000"),
                null,
                10,
                null,
                seller
        );

        SellerOrder sellerOrder = SellerOrder.create(seller, "ORD20260502-001-1");
        sellerOrder.addOrderProduct(OrderProduct.create(1, new BigDecimal("10000"), product));
        return sellerOrder;
    }

    private SellerOrder newReadySellerOrder() {
        SellerOrder sellerOrder = newPendingSellerOrder();
        sellerOrder.pay();
        return sellerOrder;
    }
}
