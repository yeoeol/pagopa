package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import com.commerce.pagopa.payment.application.port.PaymentProperties;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.OrderCannotPayException;
import com.commerce.pagopa.global.exception.PaymentCancelException;
import com.commerce.pagopa.global.exception.PaymentConfirmException;
import com.commerce.pagopa.global.response.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
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
    private PaymentGateway paymentGateway;

    @Mock
    private RestClient tossRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        PaymentTransactionService paymentTransactionService =
                new PaymentTransactionService(orderRepository, paymentRepository);
        paymentService = new PaymentService(
                paymentRepository,
                orderRepository,
                paymentProperties,
                tossRestClient,
                paymentGateway,
                paymentTransactionService
        );
    }

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

        verify(paymentGateway, never()).confirm(anyString(), any(BigDecimal.class), anyString());
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
        verify(paymentGateway, never()).confirm(anyString(), any(BigDecimal.class), anyString());
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

    @Test
    void cancelPayment_fullAmount_marksCancelledAfterTossSuccess() {
        Order order = createOrder("order-6");
        Payment payment = createPayment(order, PaymentStatus.PAID);
        mockTossCancelSuccess("paid-key", amount(10000), "사용자 요청");

        paymentService.cancelPayment(payment, amount(10000), "사용자 요청");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCancelledAmount()).isEqualByComparingTo("10000");
    }

    @Test
    void cancelPayment_partialAmount_marksPartialCancelledAfterTossSuccess() {
        Order order = createOrder("order-7");
        Payment payment = createPayment(order, PaymentStatus.PAID);
        mockTossCancelSuccess("paid-key", amount(4000), "셀러1 취소");

        paymentService.cancelPayment(payment, amount(4000), "셀러1 취소");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);
        assertThat(payment.getCancelledAmount()).isEqualByComparingTo("4000");
    }

    @Test
    void cancelPayment_consecutivePartials_finalCallPromotesToCancelled() {
        Order order = createOrder("order-8");
        Payment payment = createPayment(order, PaymentStatus.PAID);
        mockTossCancelSuccess("paid-key", amount(6000), "셀러2 취소");

        // 사전: 4000 부분 취소된 상태
        payment.cancel(amount(4000));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);

        // 추가 6000 → 누적 10000 → 전체 취소로 승격
        paymentService.cancelPayment(payment, amount(6000), "셀러2 취소");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCancelledAmount()).isEqualByComparingTo("10000");
    }

    @Test
    void cancelPayment_throwsBeforeTossWhenAlreadyCancelled() {
        Order order = createOrder("order-9");
        Payment payment = createPayment(order, PaymentStatus.CANCELLED);

        assertThatThrownBy(() -> paymentService.cancelPayment(payment, amount(10000), "재취소"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_CANCELLED);

        verify(tossRestClient, never()).post();
    }

    @Test
    void cancelPayment_throwsBeforeTossWhenAmountExceedsRemaining() {
        Order order = createOrder("order-10");
        Payment payment = createPayment(order, PaymentStatus.PAID);
        payment.cancel(amount(7000)); // 사전에 7000 환불 누적됨

        assertThatThrownBy(() -> paymentService.cancelPayment(payment, amount(4000), "초과 취소"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_AMOUNT_INVALID);

        verify(tossRestClient, never()).post();
    }

    private void mockTossConfirmSuccess(String orderId) {
        doNothing().when(paymentGateway).confirm(orderId, amount(10000), "payment-key");
    }

    private void mockTossConfirmFailure(String orderId) {
        doThrow(new RuntimeException("toss failure"))
                .when(paymentGateway).confirm(orderId, amount(10000), "payment-key");
    }

    private void mockTossCancelSuccess(String paymentKey, BigDecimal cancelAmount, String reason) {
        Map<String, String> payload = Map.of(
                "cancelReason", reason,
                "cancelAmount", cancelAmount.toString()
        );
        when(tossRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/v1/payments/{paymentKey}/cancel", paymentKey)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(payload)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
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
            payment.markInProgress();
            payment.success("paid-key");
            payment.cancel(payment.getAmount()); // 전체 환불 처리로 CANCELLED
        }

        return payment;
    }

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }
}
