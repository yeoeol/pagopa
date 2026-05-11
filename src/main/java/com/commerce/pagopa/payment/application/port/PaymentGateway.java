package com.commerce.pagopa.payment.application.port;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentConfirmResult confirm(String orderId, BigDecimal amount, String paymentKey);

    PaymentCancelResult cancel(String paymentKey, BigDecimal cancelAmount, String cancelReason);
}
