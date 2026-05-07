package com.commerce.pagopa.payment.domain.model;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void cancel_setsCancelled() {
        Payment payment = newPaidPayment();

        payment.cancel();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void cancelPartial_setsPartialCancelled() {
        Payment payment = newPaidPayment();

        payment.cancelPartial();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);
    }

    @Test
    void validateCancelable_allowsPaid() {
        Payment payment = newPaidPayment();

        payment.validateCancelable();
    }

    @Test
    void validateCancelable_allowsPartialCancelled() {
        Payment payment = newPaidPayment();
        payment.cancelPartial();

        payment.validateCancelable();
    }

    @Test
    void validateCancelable_throwsWhenAlreadyCancelled() {
        Payment payment = newPaidPayment();
        payment.cancel();

        assertThatThrownBy(payment::validateCancelable)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_CANCELLED);
    }

    @Test
    void validateCancelable_throwsWhenAlreadyFailed() {
        Payment payment = newPaidPayment();
        payment.fail();

        assertThatThrownBy(payment::validateCancelable)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_FAILED);
    }

    @Test
    void validateCancelable_throwsWhenStillReady() {
        Payment payment = Payment.create(newOrderWithAmount(10000));

        assertThatThrownBy(payment::validateCancelable)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_CANCELABLE);
    }

    private Payment newPaidPayment() {
        Payment payment = Payment.create(newOrderWithAmount(10000));
        payment.markInProgress();
        payment.success("payment-key");
        return payment;
    }

    private Order newOrderWithAmount(long amount) {
        Address address = new Address("12345", "주소", "상세");
        Delivery delivery = Delivery.create(address, "수령인", "01012345678", null);
        Order order = Order.init("order-1", PaymentMethod.CARD, null, delivery);

        SellerOrder so = SellerOrder.create(null, "order-1-1");
        ReflectionTestUtils.setField(so, "sellerTotalAmount", BigDecimal.valueOf(amount));
        order.addSellerOrder(so);

        return order;
    }
}
