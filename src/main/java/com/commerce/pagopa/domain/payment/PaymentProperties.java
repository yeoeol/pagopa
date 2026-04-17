package com.commerce.pagopa.domain.payment;

public interface PaymentProperties {
    String getBaseUrl();

    String getSecretKey();

    String getSuccessUrl();

    String getFailUrl();
}
