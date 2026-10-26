package com.commerce.pagopa.payment.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.ordering.api.OrderPaymentApi;
import com.commerce.pagopa.ordering.api.OrderPaymentSummary;
import com.commerce.pagopa.payment.application.dto.command.PaymentApprovalCommand;
import com.commerce.pagopa.payment.application.dto.command.PaymentCancelCommand;
import com.commerce.pagopa.payment.application.dto.command.PaymentRequestCommand;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.domain.Payment;
import com.commerce.pagopa.payment.domain.PaymentRepository;

/** 결제 DB 트랜잭션 경계. 외부 PG 호출은 포함하지 않고, 호출 전 검증/중간 상태 전환과 호출 후 확정만 담당한다. */
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private static final String APPROVAL_IDEMPOTENCY_KEY_PREFIX = "payment-approval-";
    private static final String CANCELLATION_IDEMPOTENCY_KEY_PREFIX = "payment-cancellation-";

    private final OrderPaymentApi orderPaymentApi;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResult request(Long userId, PaymentRequestCommand command) {
        OrderPaymentSummary summary = orderPaymentApi.validateConfirmPayment(
                userId,
                command.orderId()
        );

        String providerOrderId = UUID.randomUUID().toString().substring(0, 20);
        return paymentRepository.findByOrderId(summary.orderId())
                .map(payment -> PaymentResult.request(
                        payment.getId(),
                        providerOrderId,
                        payment.getAmount()
                ))
                .orElseGet(() -> PaymentResult.from(
                        paymentRepository.save(
                                Payment.create(
                                        command.paymentMethod(),
                                        summary.totalAmount(),
                                        summary.orderId(),
                                        command.provider()
                                )
                        ),
                        providerOrderId
                ));
    }

    @Transactional
    public PaymentApprovalRequest prepareApproval(Long userId, PaymentApprovalCommand command) {
        Payment payment = paymentRepository.findByIdForUpdateOrThrow(command.paymentId());

        OrderPaymentSummary summary = orderPaymentApi.validateConfirmPayment(
                userId,
                payment.getOrderId()
        );

        payment.startApproval(command.amount());
        return PaymentApprovalRequest.of(
                payment.getId(),
                summary.orderId(),
                command.providerPaymentId(),
                command.providerOrderId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                APPROVAL_IDEMPOTENCY_KEY_PREFIX + payment.getId()
        );
    }

    @Transactional
    public PaymentResult completeApproval(Long paymentId, PaymentResult approval) {
        Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
        if (!payment.getProvider().name().equals(approval.provider())) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUEST_ERROR);
        }

        boolean approved = payment.approve(
                approval.providerPaymentId(),
                approval.amount(),
                approval.approvedAt()
        );
        if (approved) {
            orderPaymentApi.confirmPayment(
                    payment.getOrderId(),
                    approval.amount()
            );
        }
        return PaymentResult.from(payment, approval.providerOrderId());
    }

    @Transactional
    public PaymentCancellationRequest prepareCancellation(
            Long userId,
            PaymentCancelCommand command
    ) {
        Payment payment = paymentRepository
                .findByIdForUpdateOrThrow(command.paymentId());

        orderPaymentApi.validateCancelAfterPayment(
                userId,
                payment.getOrderId()
        );

        payment.startCancellation();
        return PaymentCancellationRequest.of(
                payment.getId(),
                payment.getProviderPaymentId(),
                payment.getAmount(),
                CANCELLATION_IDEMPOTENCY_KEY_PREFIX + payment.getId(),
                command.cancelReason()
        );
    }

    @Transactional
    public PaymentResult completeCancellation(Long paymentId, PaymentResult cancellation) {
        Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);

        boolean canceled = payment.cancel(
                cancellation.providerPaymentId(),
                cancellation.amount(),
                cancellation.canceledAt()
        );
        if (canceled) {
            orderPaymentApi.cancelAfterPayment(payment.getOrderId());
        }
        return PaymentResult.from(payment, cancellation.providerOrderId());
    }

    @Transactional
    public void markApprovalFailed(Long paymentId) {
        Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
        payment.fail();
    }

    @Transactional
    public void revertCancellation(Long paymentId) {
        Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
        payment.revertCancellation();
    }
}
