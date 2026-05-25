package com.commerce.pagopa.payment.infrastructure.tossapi;

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

    private final TossPaymentProperties tossPaymentProperties;

    @Bean
    public RestClient tossRestClient() {
        String encodedAuth = Base64.getEncoder()
                .encodeToString(
                        (tossPaymentProperties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8)
                );
        return RestClient.builder()
                .baseUrl(tossPaymentProperties.getBaseUrl())
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
