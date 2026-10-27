package com.commerce.pagopa.payment.infrastructure.gateway;


import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.application.dto.toss.TossCancelRequest;
import com.commerce.pagopa.payment.application.dto.toss.TossConfirmRequest;
import com.commerce.pagopa.payment.application.dto.toss.TossPaymentResponse;
import com.commerce.pagopa.payment.application.dto.toss.TossPaymentResponseMapper;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayIndeterminateException;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import com.commerce.pagopa.payment.domain.PaymentProvider;
import com.commerce.pagopa.payment.infrastructure.config.TossPaymentProperties;

@Component
@Profile({"!local & !test"})
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final TossPaymentResponseMapper responseMapper;

    public TossPaymentGateway(
            RestClient.Builder builder,
            TossPaymentProperties paymentProperties,
            TossPaymentResponseMapper responseMapper
    ) {
        this.restClient = builder
                .baseUrl(paymentProperties.baseUrl())
                .defaultHeaders(headers -> {
                    headers.setBasicAuth(paymentProperties.secretKey(), "");
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
        this.responseMapper = responseMapper;
    }

    @Override
    public PaymentResult approve(PaymentApprovalRequest request) {
        try {
            TossPaymentResponse response = restClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", request.idempotencyKey())
                    .body(new TossConfirmRequest(
                            request.providerPaymentId(),
                            request.providerOrderId(),
                            request.amount()
                    ))
                    .exchange(
                            (httpRequest, httpResponse) ->
                                    responseMapper.readApproval(
                                            httpRequest,
                                            httpResponse,
                                            request
                                    )
                    );

            return PaymentResult.approve(
                    request.paymentId(),
                    PaymentProvider.TOSS_PAYMENTS,
                    response.paymentKey(),
                    response.orderId(),
                    response.status(),
                    response.method(),
                    response.totalAmount(),
                    response.approvedAt().toLocalDateTime()
            );
        } catch (ResourceAccessException e) {
            throw new PaymentGatewayIndeterminateException(
                    ErrorCode.PAYMENT_CONFIRM_FAIL,
                    e.getMessage()
            );
        }
    }

    @Override
    public PaymentResult cancel(PaymentCancellationRequest request) {
        try {
            TossPaymentResponse response = restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", request.providerPaymentId())
                    .header("Idempotency-Key", request.idempotencyKey())
                    .body(new TossCancelRequest(
                            request.cancelReason(),
                            request.amount()
                    ))
                    .exchange(responseMapper::readCancellation);

            return PaymentResult.cancel(
                    request.paymentId(),
                    PaymentProvider.TOSS_PAYMENTS,
                    response.paymentKey(),
                    response.orderId(),
                    response.status(),
                    response.method(),
                    response.totalAmount(),
                    response.cancels()
                            .getLast()
                            .canceledAt()
                            .toLocalDateTime()
            );
        } catch (ResourceAccessException e) {
            throw new PaymentGatewayIndeterminateException(
                    ErrorCode.PAYMENT_CANCEL_FAIL,
                    e.getMessage()
            );
        }
    }

    // idempotencyKey -> paymentKey 또는 orderId
    public TossPaymentResponse findByPaymentKey(String paymentKey) {
        try {
            return restClient.get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .exchange(responseMapper::read);
        } catch (ResourceAccessException e) {
            throw new PaymentGatewayIndeterminateException(
                    ErrorCode.PAYMENT_READ_FAILED,
                    e.getMessage()
            );
        }
    }

    public TossPaymentResponse findByOrderId(String orderId) {
        try {
            return restClient.get()
                    .uri("/v1/payments/orders/{orderId}", orderId)
                    .exchange(responseMapper::read);
        } catch (ResourceAccessException e) {
            throw new PaymentGatewayIndeterminateException(
                    ErrorCode.PAYMENT_READ_FAILED,
                    e.getMessage()
            );
        }
    }
}
