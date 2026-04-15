package com.commerce.pagopa.global.config.properties;

import com.commerce.pagopa.domain.payment.PaymentProperties;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "payment.toss")
public class TossPaymentProperties implements PaymentProperties {

    private String secretKey;
    private String baseUrl;
    private String successUrl;
    private String failUrl;

    public TossPaymentProperties(String secretKey, String baseUrl, String successUrl, String failUrl) {
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
        this.successUrl = successUrl;
        this.failUrl = failUrl;
    }
}
