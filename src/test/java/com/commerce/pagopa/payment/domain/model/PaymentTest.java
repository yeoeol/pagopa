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
    void cancel_partialAmount_setsPartialCancelledAndAccumulates() {
        Payment payment = newPaidPayment(10000);

        payment.cancel(BigDecimal.valueOf(3000));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);
        assertThat(payment.getCancelledAmount()).isEqualByComparingTo("3000");
    }

    @Test
    void cancel_partialThenRemainder_promotesToCancelled() {
        Payment payment = newPaidPayment(10000);
        payment.cancel(BigDecimal.valueOf(3000));

        payment.cancel(BigDecimal.valueOf(7000));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCancelledAmount()).isEqualByComparingTo("10000");
    }

    @Test
    void cancel_fullAmountAtOnce_setsCancelled() {
        Payment payment = newPaidPayment(10000);

        payment.cancel(BigDecimal.valueOf(10000));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCancelledAmount()).isEqualByComparingTo("10000");
    }

    @Test
    void cancel_throwsWhenAmountExceedsRemaining() {
        Payment payment = newPaidPayment(10000);
        payment.cancel(BigDecimal.valueOf(7000));

        assertThatThrownBy(() -> payment.cancel(BigDecimal.valueOf(4000)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_AMOUNT_INVALID);
    }

    @Test
    void cancel_throwsWhenAmountIsZeroOrNegative() {
        Payment payment = newPaidPayment(10000);

        assertThatThrownBy(() -> payment.cancel(BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_AMOUNT_INVALID);

        assertThatThrownBy(() -> payment.cancel(BigDecimal.valueOf(-100)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_AMOUNT_INVALID);
    }

    @Test
    void cancel_throwsWhenAlreadyFullyCancelled() {
        Payment payment = newPaidPayment(10000);
        payment.cancel(BigDecimal.valueOf(10000));

        assertThatThrownBy(() -> payment.cancel(BigDecimal.valueOf(1000)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_CANCELLED);
    }

    @Test
    void cancel_throwsWhenAlreadyFailed() {
        Payment payment = newPaidPayment(10000);
        payment.fail();

        assertThatThrownBy(() -> payment.cancel(BigDecimal.valueOf(1000)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_FAILED);
    }

    @Test
    void cancel_throwsWhenStillReady() {
        Payment payment = Payment.create(newOrderWithAmount(10000));

        assertThatThrownBy(() -> payment.cancel(BigDecimal.valueOf(1000)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_CANCELABLE);
    }

    @Test
    void cancelUnpaid_setsCancelledForReadyOrInProgress() {
        Payment readyPayment = Payment.create(newOrderWithAmount(10000));
        readyPayment.cancelUnpaid();
        assertThat(readyPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(readyPayment.getCancelledAmount()).isEqualByComparingTo("0"); // 누적 금액은 변동 없음

        Payment inProgress = Payment.create(newOrderWithAmount(10000));
        inProgress.markInProgress();
        inProgress.cancelUnpaid();
        assertThat(inProgress.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void cancelUnpaid_throwsWhenAlreadyPaid() {
        Payment payment = newPaidPayment(10000);

        assertThatThrownBy(payment::cancelUnpaid)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_CANCELABLE);
    }

    @Test
    void create_initializesCancelledAmountToZero() {
        Payment payment = Payment.create(newOrderWithAmount(10000));

        assertThat(payment.getCancelledAmount()).isEqualByComparingTo("0");
    }

    private Payment newPaidPayment(long amount) {
        Payment payment = Payment.create(newOrderWithAmount(amount));
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
