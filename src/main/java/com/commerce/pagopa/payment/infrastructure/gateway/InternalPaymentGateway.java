package com.commerce.pagopa.payment.infrastructure.gateway;

import com.commerce.pagopa.payment.application.dto.request.CancelPaymentCommand;
import com.commerce.pagopa.payment.application.dto.request.PaymentCommand;
import com.commerce.pagopa.payment.application.dto.response.PaymentApproval;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellation;
import com.commerce.pagopa.payment.application.port.PaymentGateway;

public class InternalPaymentGateway implements PaymentGateway {

	@Override
	public PaymentApproval approve(PaymentCommand command) {
		return null;
	}

	@Override
	public PaymentCancellation cancel(CancelPaymentCommand command) {
		return null;
	}
}
