package com.commerce.pagopa.payment.application.port;

import java.math.BigDecimal;

public interface PaymentGateway {
    void confirm(String orderId, BigDecimal amount, String paymentKey);
}
