package com.commerce.pagopa.payment.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayRejectedException;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import com.commerce.pagopa.payment.event.PaymentCancellationRequested;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancellationEventListener {

    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;

    @ApplicationModuleListener(
            id = "payment.cancellation",
            propagation = Propagation.NOT_SUPPORTED
    )
    void on(PaymentCancellationRequested event) {
        try {
            PaymentResult cancellation = paymentGateway.cancel(event.toRequest());

            paymentService.completeCancellation(
                    event.paymentId(),
                    cancellation
            );
        } catch (PaymentGatewayRejectedException exception) {
            paymentService.revertCancellation(event.paymentId());

            log.info(
                    "Payment cancellation rejected. " +
                    "paymentId={}, message={}",
                    event.paymentId(),
                    exception.getMessage()
            );
        }
    }
}
