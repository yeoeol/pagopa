package com.commerce.pagopa.payment.application.port;

public interface PaymentProperties {
    String getBaseUrl();

    String getSecretKey();

    String getSuccessUrl();

    String getFailUrl();
}
