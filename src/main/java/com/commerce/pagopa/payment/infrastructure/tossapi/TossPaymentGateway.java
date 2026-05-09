package com.commerce.pagopa.payment.infrastructure.tossapi;

import com.commerce.pagopa.payment.application.port.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;

    @Override
    public void confirm(String orderId, BigDecimal amount, String paymentKey) {
        Map<String, String> payload = Map.of(
                "orderId", orderId,
                "amount", amount.toString(),
                "paymentKey", paymentKey
        );

        tossRestClient.post()
                .uri("/v1/payments/confirm")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}