package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.payment.application.dto.response.PaymentResponseDto;
import com.commerce.pagopa.payment.application.port.PaymentCancelResult;
import com.commerce.pagopa.payment.application.port.PaymentConfirmResult;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
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

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentProperties paymentProperties;
    private final PaymentGateway paymentGateway;
    private final PaymentTransactionService paymentTransactionService;

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
    public void confirmPayment(PaymentApproveRequestDto requestDto) {
        boolean canProceed = paymentTransactionService.prepareConfirm(requestDto);
        if (!canProceed) {
            throw new PaymentCancelException();
        }

        PaymentConfirmResult result;
        try {
            result = paymentGateway.confirm(requestDto.orderId(), requestDto.amount(), requestDto.paymentKey());
        } catch (Exception e) {
            log.error("[Payment] 토스 승인 API 호출 실패 - orderNumber={}", requestDto.orderId(), e);
            paymentTransactionService.markConfirmFailure(requestDto.orderId());
            throw new PaymentConfirmException();
        }

        if (!result.success()) {
            log.error("[Payment] 토스 승인 응답 거절 - orderNumber={}, status={}",
                    requestDto.orderId(), result.status());
            paymentTransactionService.markConfirmFailure(requestDto.orderId());
            throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_REJECTED);
        }

        paymentTransactionService.markConfirmSuccess(requestDto.orderId(), requestDto.paymentKey());
        log.info("[Payment] 승인 성공 - orderNumber={}, paymentKey={}", requestDto.orderId(), requestDto.paymentKey());
    }

    /**
     * 승인된 결제를 paymentKey로 환불 취소 (Toss API 호출 + 누적 환불 금액 갱신)
     */
    public void cancelPayment(Payment payment, BigDecimal cancelAmount, String cancelReason) {
        PaymentCancelCommand command = paymentTransactionService.prepareCancel(payment, cancelAmount);

        PaymentCancelResult result;
        try {
            result = paymentGateway.cancel(command.paymentKey(), command.cancelAmount(), cancelReason);
        } catch (Exception e) {
            log.error("[Payment] 토스 결제 취소 API 호출 실패 - paymentKey={}, cancelAmount={}",
                    command.paymentKey(), command.cancelAmount(), e);
            releaseCancelLockSafely(command.paymentId());
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAIL);
        }

        if (!result.success()) {
            log.error("[Payment] 토스 결제 취소 응답 거절 - paymentKey={}, status={}",
                    command.paymentKey(), result.status());
            releaseCancelLockSafely(command.paymentId());
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_REJECTED);
        }

        paymentTransactionService.markCancelSuccess(command.paymentId(), command.cancelAmount());
        log.info("[Payment] 결제 취소 성공 - paymentKey={}, cancelAmount={}",
                command.paymentKey(), command.cancelAmount());
    }

    /**
     * Toss 미승인 결제를 로컬에서만 취소 (스케줄러용 — paymentKey 없는 READY/IN_PROGRESS 건)
     */
    @Transactional
    public void cancelPaymentByOrder(Order order) {
        paymentRepository.findByOrder(order)
                .filter(p -> p.getPaymentKey() == null
                        && (p.getStatus() == PaymentStatus.READY
                            || p.getStatus() == PaymentStatus.IN_PROGRESS))
                .ifPresent(Payment::cancelUnpaid);
    }

    /**
     * 취소 락 해제 — 정상 시 1 반환, 0이면 락 상태 불일치(이미 다른 곳에서 전이됨)
     * 운영자 인지용 WARN만 남기고 원본 예외(PAYMENT_CANCEL_FAIL/REJECTED)는 그대로 전파
     */
    private void releaseCancelLockSafely(Long paymentId) {
        int released = paymentTransactionService.releaseCancelLock(paymentId);
        if (released == 0) {
            log.warn("[Payment] 취소 락 해제 시 영향 행이 0 — 락 상태 불일치 가능. paymentId={}", paymentId);
        }
    }
}
