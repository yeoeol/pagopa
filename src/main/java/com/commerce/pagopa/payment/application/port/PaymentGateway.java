package com.commerce.pagopa.payment.application.port;

import com.commerce.pagopa.payment.application.dto.request.CancelPaymentCommand;
import com.commerce.pagopa.payment.application.dto.request.PaymentCommand;
import com.commerce.pagopa.payment.application.dto.response.PaymentApproval;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellation;

public interface PaymentGateway {
	PaymentApproval approve(PaymentCommand command);
	PaymentCancellation cancel(CancelPaymentCommand command);
}
