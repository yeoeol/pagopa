package com.commerce.pagopa.payment.application.port;

import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;

public interface PaymentGateway {
	PaymentApprovalResponse approve(PaymentApprovalRequest request);
	PaymentCancellationResponse cancel(PaymentCancellationRequest request);
}
