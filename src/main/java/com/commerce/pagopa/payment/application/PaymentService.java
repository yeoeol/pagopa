package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.payment.application.dto.response.PaymentResponseDto;
import com.commerce.pagopa.payment.application.port.PaymentProperties;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
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
        order.validatePayable();

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
        Order order = orderRepository.getByOrderNumberOrThrow(requestDto.orderId());

        Payment payment = paymentRepository.getByOrderOrThrow(order);

        payment.validateConfirmable();
        order.validatePayable();

        if (!payment.isAmountMatched(requestDto.amount())) {
            payment.fail();
            order.cancel();
            throw new PaymentCancelException();
        }

        callTossConfirmApi(requestDto, payment, order);
    }

    /**
     * 승인된 결제를 paymentKey로 취소 (유저 요청용, Toss API 호출).
     * cancelAmount는 항상 명시 — 전체 취소 시에는 payment.getAmount() 전달, 부분 취소 시에는 해당 금액 전달
     */
    @Transactional
    public void cancelPayment(Payment payment, BigDecimal cancelAmount, String cancelReason) {
        payment.validateCancelable();
        callTossCancelApi(cancelReason, cancelAmount, payment);
    }

    /**
     * Toss 미승인 결제를 로컬에서만 취소 (스케줄러용 — paymentKey 없는 READY/IN_PROGRESS 건)
     */
    @Transactional
    public void cancelPaymentByOrder(Order order) {
        paymentRepository.findByOrder(order)
                .filter(p -> p.getStatus() != PaymentStatus.PAID)
                .ifPresent(Payment::cancel);
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
            order.cancel();
            log.error("[Payment] 토스 API 호출 실패 - orderNumber={}", requestDto.orderId(), e);
            throw new PaymentConfirmException();
        }

        payment.success(requestDto.paymentKey());
        order.pay();
        log.info("[Payment] 승인 성공 - orderNumber={}, paymentKey={}", requestDto.orderId(), requestDto.paymentKey());
    }

    private void callTossCancelApi(String reason, BigDecimal cancelAmount, Payment payment) {
        Map<String, String> payload = Map.of(
                "cancelReason", reason,
                "cancelAmount", cancelAmount.toString()
        );

        String paymentKey = payment.getPaymentKey();
        try {
            tossRestClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("[Payment] 토스 결제 취소 API 호출 실패 - paymentKey={}, cancelAmount={}", paymentKey, cancelAmount, e);
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAIL);
        }

        payment.cancel();
        log.info("[Payment] 결제 취소 성공 - paymentKey={}, cancelAmount={}, reason={}", paymentKey, cancelAmount, reason);
    }
}
