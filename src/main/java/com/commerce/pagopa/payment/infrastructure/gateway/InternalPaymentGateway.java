package com.commerce.pagopa.payment.infrastructure.gateway;

import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;
import com.commerce.pagopa.payment.application.port.PaymentGateway;

public class InternalPaymentGateway implements PaymentGateway {

	@Override
	public PaymentApprovalResponse approve(PaymentApprovalRequest request) {
		return null;
	}

	@Override
	public PaymentCancellationResponse cancel(PaymentCancellationRequest request) {
		return null;
	}
}
