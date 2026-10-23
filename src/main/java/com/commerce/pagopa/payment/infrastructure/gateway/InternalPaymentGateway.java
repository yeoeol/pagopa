package com.commerce.pagopa.payment.infrastructure.gateway;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayRejectedException;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import com.commerce.pagopa.payment.domain.PaymentProvider;

/** 서버 내부 테스트용 결제 서비스. idempotencyKey 기준으로 승인/취소 결과를 보관해 조회·재시도를 지원한다. */
@Component
@Profile({"local", "test"})
public class InternalPaymentGateway implements PaymentGateway {

    private final ConcurrentHashMap<String, PaymentResult> repository = new ConcurrentHashMap<>();

    @Override
    public PaymentResult approve(PaymentApprovalRequest request) {
        return repository.computeIfAbsent(
                request.paymentId().toString(),
                key -> PaymentResult.approve(
                        request.paymentId(),
                        PaymentProvider.INTERNAL,
                        "INTERNAL-" + key,
                        "DONE",
                        request.paymentMethod(),
                        request.amount(),
                        LocalDateTime.now()
                )
        );
    }

    @Override
    public PaymentResult cancel(PaymentCancellationRequest request) {
        String key = request.paymentId().toString();

        return repository.compute(
                key,
                (ignored, current) -> {
                    if (current == null) {
                        throw new PaymentGatewayRejectedException(
                                ErrorCode.PAYMENT_CANCEL_REJECTED
                        );
                    }
                    if (!Objects.equals(
                            current.providerPaymentId(),
                            request.providerPaymentId()
                    )) {
                        throw new PaymentGatewayRejectedException(
                                ErrorCode.PAYMENT_CANCEL_REJECTED
                        );
                    }

                    if (!Objects.equals(
                            current.amount(),
                            request.amount()
                    )) {
                        throw new PaymentGatewayRejectedException(
                                ErrorCode.PAYMENT_CANCEL_AMOUNT_INVALID
                        );
                    }

                    return current.withCancellation(LocalDateTime.now());
                }
        );
    }
}
