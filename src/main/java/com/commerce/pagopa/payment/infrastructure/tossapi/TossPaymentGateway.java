package com.commerce.pagopa.payment.infrastructure.tossapi;

import com.commerce.pagopa.payment.application.port.PaymentCancelResult;
import com.commerce.pagopa.payment.application.port.PaymentConfirmResult;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_DONE = "DONE";
    private static final String CANCEL_DONE = "CANCELED";
    private static final String CANCEL_PARTIAL_DONE = "PARTIAL_CANCELED";

    private final RestClient tossRestClient;

    @Override
    public PaymentConfirmResult confirm(String orderId, BigDecimal amount, String paymentKey) {
        Map<String, String> payload = Map.of(
                "orderId", orderId,
                "amount", amount.toString(),
                "paymentKey", paymentKey
        );

        TossConfirmResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .body(payload)
                .retrieve()
                .body(TossConfirmResponse.class);

        return toConfirmResult(response);
    }

    @Override
    public PaymentCancelResult cancel(String paymentKey, BigDecimal cancelAmount, String cancelReason) {
        Map<String, String> payload = Map.of(
                "cancelReason", cancelReason,
                "cancelAmount", cancelAmount.toString()
        );

        TossCancelResponse response = tossRestClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .body(payload)
                .retrieve()
                .body(TossCancelResponse.class);

        return toCancelResult(response);
    }

    private PaymentConfirmResult toConfirmResult(TossConfirmResponse response) {
        if (response == null) {
            return new PaymentConfirmResult(false, null, null, null, null);
        }
        boolean success = CONFIRM_DONE.equals(response.status());
        return new PaymentConfirmResult(
                success,
                response.status(),
                response.paymentKey(),
                response.totalAmount(),
                response.approvedAt()
        );
    }

    private PaymentCancelResult toCancelResult(TossCancelResponse response) {
        if (response == null) {
            return new PaymentCancelResult(false, null, null, null, null);
        }
        boolean success = CANCEL_DONE.equals(response.status())
                || CANCEL_PARTIAL_DONE.equals(response.status());
        TossCancelResponse.Cancel latest = latestCancel(response);
        return new PaymentCancelResult(
                success,
                response.status(),
                latest != null ? latest.cancelAmount() : null,
                response.balanceAmount(),
                latest != null ? latest.canceledAt() : null
        );
    }

    private TossCancelResponse.Cancel latestCancel(TossCancelResponse response) {
        if (response.cancels() == null || response.cancels().isEmpty()) {
            return null;
        }
        return response.cancels().getLast();
    }
}
