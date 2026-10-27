package com.commerce.pagopa.payment.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayRejectedException;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import com.commerce.pagopa.payment.event.PaymentApprovalRequested;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApprovalEventListener {

    private final PaymentGateway paymentGateway;
    private final PaymentTransactionService paymentTransactionService;

    @ApplicationModuleListener(
            id = "payment.approval",
            propagation = Propagation.NOT_SUPPORTED
    )
    void on(PaymentApprovalRequested event) {
        try {
            PaymentResult approval = paymentGateway.approve(event.toRequest());

            paymentTransactionService.completeApproval(
                    event.paymentId(),
                    approval
            );
        } catch (PaymentGatewayRejectedException exception) {
            paymentTransactionService.markApprovalFailed(event.paymentId());

            log.info(
                    "Payment approval rejected. paymentId={}, message={}",
                    event.paymentId(),
                    exception.getMessage()
            );
        }
    }
}
