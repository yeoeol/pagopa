package com.commerce.pagopa.domain.payment.service;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.payment.PaymentProperties;
import com.commerce.pagopa.domain.payment.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.domain.order.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.domain.payment.dto.response.PaymentResponseDto;
import com.commerce.pagopa.domain.payment.entity.Payment;
import com.commerce.pagopa.domain.payment.repository.PaymentRepository;
import com.commerce.pagopa.global.exception.*;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentProperties paymentProperties;
    private final RestClient tossRestClient;

    /**
     * 주문 생성 직후, 결제 데이터를 준비하고 클라이언트(프론트엔드)가 토스 창을 띄울 수 있도록 DTO 반환
     */
    @Transactional
    public PaymentResponseDto requestPayment(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        validateOrderPayable(order);

        Payment payment = paymentRepository.findByOrder(order)
                .orElseGet(() -> paymentRepository.save(Payment.create(order)));

        payment.markInProgress();

        return PaymentResponseDto.of(
                order.getOrderNumber(),
                order.getOrderName(),
                paymentProperties.getSuccessUrl(),
                paymentProperties.getFailUrl(),
                order.getTotalAmount(),
                order.getDelivery().getRecipientName()
        );
    }

    /**
     * 토스 결제 창에서 승인 후 Redirect로 돌아왔을 때, 토스 서버로 최종 결제 승인 API 호출
     */
    @Transactional(noRollbackFor = {PaymentCancelException.class, PaymentConfirmException.class})
    public void confirmPayment(PaymentApproveRequestDto requestDto) {
        Order order = orderRepository.getByOrderNumber(requestDto.orderId());

        Payment payment = paymentRepository.getByOrderOrThrow(order);

        payment.validateConfirmable();
        validateOrderPayable(order);
        validateAmount(requestDto.amount(), payment, order);

        callTossConfirmApi(requestDto, payment, order);
    }

    /**
     * 승인된 결제를 paymentKey로 취소
     */
    @Transactional
    public void cancelPayment(String paymentKey, String cancelReason) {
        Payment payment = paymentRepository.getByPaymentKeyOrThrow(paymentKey);

        payment.validateCancelable();

        callTossCancelApi(cancelReason, payment);
    }

    private void validateAmount(BigDecimal amount, Payment payment, Order order) {
        if (payment.getAmount().compareTo(amount) != 0) {
            payment.fail();
            order.markAsCancelled();
            throw new PaymentCancelException();
        }
    }

    private void callTossConfirmApi(PaymentApproveRequestDto requestDto, Payment payment, Order order) {
        Map<String, String> payload = Map.of(
                "orderId", requestDto.orderId(),
                "amount", requestDto.amount().toString(),
                "paymentKey", requestDto.paymentKey()
        );

        try {
            tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            payment.fail();
            order.markAsCancelled();
            log.error("[Payment] 토스 API 호출 실패 - orderNumber={}", requestDto.orderId(), e);
            throw new PaymentConfirmException();
        }

        payment.success(requestDto.paymentKey());
        order.markAsPaid();
        log.info("[Payment] 승인 성공 - orderNumber={}, paymentKey={}", requestDto.orderId(), requestDto.paymentKey());
    }

    private void callTossCancelApi(String reason, Payment payment) {
        Map<String, String> payload = Map.of(
                "cancelReason", reason
        );

        String paymentKey = payment.getPaymentKey();
        try {
            tossRestClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("[Payment] 토스 결제 취소 API 호출 실패 - paymentKey={}", paymentKey, e);
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAIL);
        }

        payment.cancel();
        log.info("[Payment] 결제 취소 성공 - paymentKey={}, reason={}", paymentKey, reason);
    }

    private static void validateOrderPayable(Order order) {
        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new OrderCannotPayException();
        }
    }
}
