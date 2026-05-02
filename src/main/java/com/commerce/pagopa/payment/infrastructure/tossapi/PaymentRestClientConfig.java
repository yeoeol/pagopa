package com.commerce.pagopa.payment.infrastructure.tossapi;

import com.commerce.pagopa.payment.application.port.PaymentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentRestClientConfig {

    private final PaymentProperties paymentProperties;

    @Bean
    public RestClient tossRestClient() {
        String encodedAuth = Base64.getEncoder()
                .encodeToString(
                        (paymentProperties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8)
                );
        return RestClient.builder()
                .baseUrl(paymentProperties.getBaseUrl())
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
