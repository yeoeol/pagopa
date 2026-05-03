package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.payment.application.port.PaymentProperties;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
        order.cancel();

        when(orderRepository.findByIdOrThrow(1L)).thenReturn(order);

        assertThatThrownBy(() -> paymentService.requestPayment(1L))
                .isInstanceOf(OrderCannotPayException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_CANNOT_PAY);

        verify(paymentRepository, never()).findByOrder(order);
    }

    @Test
    void confirmPayment_throwsAlreadyCompletedBeforeCallingToss() {
        Order order = createOrder("order-2");
        order.pay();
        Payment payment = createPayment(order, PaymentStatus.PAID);

        when(orderRepository.getByOrderNumberOrThrow("order-2")).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);

        assertThatThrownBy(() -> paymentService.confirmPayment(
                new PaymentApproveRequestDto("paid-key", "order-2", amount(10000)))
        ).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_COMPLETED);

        verify(tossRestClient, never()).post();
    }

    @Test
    void confirmPayment_marksPaymentFailedWhenAmountDoesNotMatch() {
        Order order = createOrder("order-3");
        Payment payment = createPayment(order, PaymentStatus.IN_PROGRESS);

        when(orderRepository.getByOrderNumberOrThrow("order-3")).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);

        assertThatThrownBy(() -> paymentService.confirmPayment(
                new PaymentApproveRequestDto("payment-key", "order-3", amount(9000)))
        ).isInstanceOf(PaymentCancelException.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(tossRestClient, never()).post();
    }

    @Test
    void confirmPayment_marksPaymentFailedWhenTossConfirmFails() {
        Order order = createOrder("order-4");
        Payment payment = createPayment(order, PaymentStatus.IN_PROGRESS);

        when(orderRepository.getByOrderNumberOrThrow("order-4")).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);
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

        when(orderRepository.getByOrderNumberOrThrow("order-5")).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);
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
        Address address = new Address("12345", "주소", "상세");
        Delivery delivery = Delivery.create(address, "수령인", "01012345678", null);
        Order order = Order.init(orderNumber, PaymentMethod.CARD, null, delivery);
        ReflectionTestUtils.setField(order, "orderName", "테스트 주문");

        // SellerOrder 1건을 부착해 totalAmount=10000 으로 만든다 (테스트 시나리오용)
        SellerOrder so = SellerOrder.create(null, orderNumber + "-1");
        ReflectionTestUtils.setField(so, "sellerTotalAmount", amount(10000));
        order.addSellerOrder(so);

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
