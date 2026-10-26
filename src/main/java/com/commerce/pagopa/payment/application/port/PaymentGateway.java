package com.commerce.pagopa.payment.application.port;

import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;

public interface PaymentGateway {

    PaymentResult approve(PaymentApprovalRequest request);

    PaymentResult cancel(PaymentCancellationRequest request);
}
