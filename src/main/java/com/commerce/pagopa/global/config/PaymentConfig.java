package com.commerce.pagopa.global.config;

import com.commerce.pagopa.global.config.properties.TossPaymentProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentConfig {
}
