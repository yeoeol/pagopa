package com.commerce.pagopa.domain.payment.service;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.payment.PaymentProperties;
import com.commerce.pagopa.domain.payment.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.domain.payment.entity.Payment;
import com.commerce.pagopa.domain.payment.entity.enums.PaymentStatus;
import com.commerce.pagopa.domain.payment.repository.PaymentRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.OrderCannotPayException;
import com.commerce.pagopa.global.exception.PaymentCancelException;
import com.commerce.pagopa.global.exception.PaymentConfirmException;
import com.commerce.pagopa.global.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentProperties paymentProperties;

    @Mock
    private RestClient tossRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void requestPayment_throwsWhenOrderIsCancelled() {
        Order order = createOrder("order-1");
        order.markAsCancelled();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.requestPayment(1L))
                .isInstanceOf(OrderCannotPayException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_CANNOT_PAY);

        verify(paymentRepository, never()).findByOrder(order);
    }

    @Test
    void confirmPayment_throwsAlreadyCompletedBeforeCallingToss() {
        Order order = createOrder("order-2");
        order.markAsPaid();
        Payment payment = createPayment(order, PaymentStatus.PAID);

        when(orderRepository.findByOrderNumber("order-2")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirmPayment(new PaymentApproveRequestDto("paid-key", "order-2", amount(10000))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_COMPLETED);

        verify(tossRestClient, never()).post();
    }

    @Test
    void confirmPayment_marksPaymentFailedWhenAmountDoesNotMatch() {
        Order order = createOrder("order-3");
        Payment payment = createPayment(order, PaymentStatus.IN_PROGRESS);

        when(orderRepository.findByOrderNumber("order-3")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirmPayment(new PaymentApproveRequestDto("payment-key", "order-3", amount(9000))))
                .isInstanceOf(PaymentCancelException.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(tossRestClient, never()).post();
    }

    @Test
    void confirmPayment_marksPaymentFailedWhenTossConfirmFails() {
        Order order = createOrder("order-4");
        Payment payment = createPayment(order, PaymentStatus.IN_PROGRESS);

        when(orderRepository.findByOrderNumber("order-4")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));
        mockTossConfirmFailure("order-4");

        assertThatThrownBy(() -> paymentService.confirmPayment(new PaymentApproveRequestDto("payment-key", "order-4", amount(10000))))
                .isInstanceOf(PaymentConfirmException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CONFIRM_FAIL);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void confirmPayment_marksPaymentPaidWhenTossConfirmSucceeds() {
        Order order = createOrder("order-5");
        Payment payment = createPayment(order, PaymentStatus.IN_PROGRESS);

        when(orderRepository.findByOrderNumber("order-5")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));
        mockTossConfirmSuccess("order-5");

        paymentService.confirmPayment(new PaymentApproveRequestDto("payment-key", "order-5", amount(10000)));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaymentKey()).isEqualTo("payment-key");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    private void mockTossConfirmSuccess(String orderId) {
        when(tossRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/v1/payments/confirm")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(confirmPayload(orderId))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    private void mockTossConfirmFailure(String orderId) {
        when(tossRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/v1/payments/confirm")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(confirmPayload(orderId))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RuntimeException("toss failure"));
    }

    private Order createOrder(String orderNumber) {
        Order order = Order.init(orderNumber, PaymentMethod.CARD, null);
        ReflectionTestUtils.setField(order, "totalAmount", amount(10000));
        ReflectionTestUtils.setField(order, "orderName", "테스트 주문");
        return order;
    }

    private Payment createPayment(Order order, PaymentStatus status) {
        Payment payment = Payment.create(order);

        if (status == PaymentStatus.IN_PROGRESS || status == PaymentStatus.PAID) {
            payment.markInProgress();
        }
        if (status == PaymentStatus.PAID) {
            payment.success("paid-key");
        }
        if (status == PaymentStatus.FAILED) {
            payment.fail();
        }
        if (status == PaymentStatus.CANCELLED) {
            payment.cancel();
        }

        return payment;
    }

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    private Map<String, String> confirmPayload(String orderId) {
        return Map.of(
                "orderId", orderId,
                "amount", amount(10000).toString(),
                "paymentKey", "payment-key"
        );
    }
}
