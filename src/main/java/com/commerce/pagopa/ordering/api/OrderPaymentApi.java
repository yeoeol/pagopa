package com.commerce.pagopa.ordering.api;

public interface OrderPaymentApi {

    OrderPaymentSummary validateConfirmPayment(Long userId, Long orderId);

    void validateCancelAfterPayment(Long userId, Long orderId);

    void confirmPayment(Long orderId, Integer integer);

    void cancelAfterPayment(Long orderId);
}
