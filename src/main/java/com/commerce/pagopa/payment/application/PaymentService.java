package com.commerce.pagopa.payment.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.payment.application.dto.command.PaymentApprovalCommand;
import com.commerce.pagopa.payment.application.dto.command.PaymentCancelCommand;
import com.commerce.pagopa.payment.application.dto.command.PaymentRequestCommand;
import com.commerce.pagopa.payment.application.dto.request.*;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;

/** 결제 유스케이스 오케스트레이션. DB 트랜잭션(사전 검증·중간/최종 상태)과 외부 PG 호출을 분리한다. */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionService paymentTransactionService;

    public PaymentResult request(Long userId, PaymentRequestCommand command) {
        return paymentTransactionService.request(userId, command);
    }

    public PaymentResult approve(Long userId, PaymentApprovalCommand command) {
        return paymentTransactionService.startApproval(userId, command);
    }

    public PaymentResult cancel(Long userId, PaymentCancelCommand command) {
        return paymentTransactionService.startCancellation(userId, command);
    }
}
