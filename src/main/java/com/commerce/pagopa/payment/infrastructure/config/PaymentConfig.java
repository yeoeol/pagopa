package com.commerce.pagopa.payment.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentConfig {
}
