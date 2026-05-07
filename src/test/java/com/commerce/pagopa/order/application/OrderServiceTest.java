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
import com.commerce.pagopa.payment.application.PaymentService;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartRepository cartRepository;
    @Mock private PaymentService paymentService;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks private OrderService orderService;

    @Test
    void cancelSellerOrder_partial_callsCancelPaymentPartialWhenOthersStillActive() {
        // given: 2개 SellerOrder가 모두 READY
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder so1 = order.getSellerOrders().get(0);
        SellerOrder so2 = order.getSellerOrders().get(1);
        Payment payment = newPaidPayment(order);

        when(orderRepository.findByIdOrThrow(1L)).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);

        // when: SellerOrder 1 취소
        orderService.cancelSellerOrder(1L, so1.getId(), new OrderCancelRequestDto("사이즈 변경"));

        // then: SellerOrder 1만 CANCELLED, 2는 READY 유지
        assertThat(so1.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(so2.getStatus()).isEqualTo(SellerOrderStatus.READY);
        // Order 상태는 혼재 → DELIVERING (도메인 deriveStatus 규약)
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);

        // PaymentService.cancelPaymentPartial이 so1 금액으로 호출됨
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(paymentService).cancelPaymentPartial(eq(payment), amountCaptor.capture(), eq("사이즈 변경"));
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(so1.getSellerTotalAmount());
        verify(paymentService, never()).cancelPayment(any(), any(), any());
    }

    @Test
    void cancelSellerOrder_lastActiveOne_callsFullCancelPaymentAndOrderBecomesCancelled() {
        // given: 2개 중 1개 이미 CANCELLED, 1개 READY
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder so1 = order.getSellerOrders().get(0);
        SellerOrder so2 = order.getSellerOrders().get(1);
        so1.cancelByBuyer(); // 사전 부분 취소
        Payment payment = newPaidPayment(order);
        payment.cancelPartial(); // 그에 따라 Payment는 PARTIAL_CANCELLED 상태

        when(orderRepository.findByIdOrThrow(1L)).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);

        // when: 마지막 남은 SellerOrder 2 취소
        orderService.cancelSellerOrder(1L, so2.getId(), new OrderCancelRequestDto("재고 없음"));

        // then: 모든 SellerOrder가 CANCELLED → Order CANCELLED → Payment 전체 취소
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(paymentService).cancelPayment(eq(payment), eq(so2.getSellerTotalAmount()), eq("재고 없음"));
        verify(paymentService, never()).cancelPaymentPartial(any(), any(), any());
    }

    @Test
    void cancelSellerOrder_throwsWhenSellerOrderNotInThisOrder() {
        Order order = newPaidOrderWithTwoSellers();

        when(orderRepository.findByIdOrThrow(1L)).thenReturn(order);

        assertThatThrownBy(() -> orderService.cancelSellerOrder(1L, 999L, new OrderCancelRequestDto("잘못된 id")))
                .isInstanceOf(SellerOrderNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELLER_ORDER_NOT_FOUND);

        verify(paymentService, never()).cancelPayment(any(), any(), any());
        verify(paymentService, never()).cancelPaymentPartial(any(), any(), any());
    }

    @Test
    void cancelSellerOrder_throwsAfterDeliveringWithoutCallingPaymentService() {
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder so1 = order.getSellerOrders().get(0);
        so1.deliver(); // 발송 시작

        when(orderRepository.findByIdOrThrow(1L)).thenReturn(order);

        assertThatThrownBy(() -> orderService.cancelSellerOrder(1L, so1.getId(), new OrderCancelRequestDto("늦은 취소")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("발송 후에는 반품으로 처리하세요");

        verify(paymentService, never()).cancelPayment(any(), any(), any());
        verify(paymentService, never()).cancelPaymentPartial(any(), any(), any());
    }

    @Test
    void cancelSellerOrder_propagatesPaymentServiceFailureToTriggerRollback() {
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder so1 = order.getSellerOrders().get(0);
        SellerOrder so2 = order.getSellerOrders().get(1);
        Payment payment = newPaidPayment(order);

        when(orderRepository.findByIdOrThrow(1L)).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);
        doThrow(new BusinessException(ErrorCode.PAYMENT_CANCEL_FAIL))
                .when(paymentService).cancelPaymentPartial(any(), any(), any());

        assertThatThrownBy(() -> orderService.cancelSellerOrder(
                1L, so1.getId(), new OrderCancelRequestDto("Toss 실패 시나리오")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_FAIL);

        // 도메인 in-memory mutation은 이미 일어난 상태 — 실제로는 @Transactional이 DB 단계에서 롤백을 수행함.
        assertThat(so1.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(so2.getStatus()).isEqualTo(SellerOrderStatus.READY);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void cancelOrder_passesRemainingAmountAfterPriorPartialCancel() {
        // given: 2개 중 1개 이미 부분 취소된 상태에서 전체 취소
        Order order = newPaidOrderWithTwoSellers();
        SellerOrder so1 = order.getSellerOrders().get(0);
        SellerOrder so2 = order.getSellerOrders().get(1);
        so1.cancelByBuyer();
        Payment payment = newPaidPayment(order);
        payment.cancelPartial();

        when(orderRepository.findByIdOrThrow(1L)).thenReturn(order);
        when(paymentRepository.getByOrderOrThrow(order)).thenReturn(payment);

        // when: 전체 주문 취소
        orderService.cancelOrder(1L, new OrderCancelRequestDto("전체 취소"));

        // then: Toss로 보낸 환불액은 잔여(so2 금액)만, 원 결제 총액(so1+so2)이 아님
        verify(paymentService).cancelPayment(eq(payment), eq(so2.getSellerTotalAmount()), eq("전체 취소"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    /** 2개의 READY SellerOrder를 가진 Order 구성. id가 필요한 테스트를 위해 ReflectionTestUtils로 id 주입. */
    private Order newPaidOrderWithTwoSellers() {
        Address address = new Address("12345", "주소", "상세");
        Delivery delivery = Delivery.create(address, "수령인", "01012345678", null);
        User buyer = newUser("buyer", Role.ROLE_USER);
        Order order = Order.init("ORD-1", PaymentMethod.CARD, buyer, delivery);
        ReflectionTestUtils.setField(order, "id", 1L);

        SellerOrder so1 = newReadySellerOrder("seller-1", "ORD-1-1", new BigDecimal("4000"));
        ReflectionTestUtils.setField(so1, "id", 11L);
        order.addSellerOrder(so1);

        SellerOrder so2 = newReadySellerOrder("seller-2", "ORD-1-2", new BigDecimal("3000"));
        ReflectionTestUtils.setField(so2, "id", 12L);
        order.addSellerOrder(so2);

        return order;
    }

    private SellerOrder newReadySellerOrder(String sellerProviderId, String sellerOrderNumber, BigDecimal price) {
        User seller = newUser(sellerProviderId, Role.ROLE_SELLER);
        Product product = Product.create(
                "product-" + sellerProviderId,
                "description",
                price,
                null,
                10,
                null,
                seller
        );
        SellerOrder so = SellerOrder.create(seller, sellerOrderNumber);
        so.addOrderProduct(OrderProduct.create(1, price, product));
        so.pay();
        return so;
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

    private Payment newPaidPayment(Order order) {
        Payment payment = Payment.create(order);
        payment.markInProgress();
        payment.success("payment-key");
        // 상태 PAID 상태로 도착하도록 status 직접 보정 — 위 흐름이 PaymentStatus.PAID로 설정함
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        return payment;
    }
}
