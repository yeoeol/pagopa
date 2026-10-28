package com.commerce.pagopa.payment.infrastructure.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.toss")
public record TossPaymentProperties(
        String secretKey,
        URI baseUrl,
        URI successUrl,
        URI failUrl
) {
}
