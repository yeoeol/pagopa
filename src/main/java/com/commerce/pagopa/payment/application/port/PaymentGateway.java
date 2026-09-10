package com.commerce.pagopa.payment.application.port;

import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;

public interface PaymentGateway {
	PaymentApprovalResponse approve(PaymentApprovalRequest request);
}
