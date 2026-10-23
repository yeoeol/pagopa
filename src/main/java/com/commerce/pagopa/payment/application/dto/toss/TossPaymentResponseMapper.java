package com.commerce.pagopa.payment.application.dto.toss;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse;
import org.springframework.web.client.RestClientException;

import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayIndeterminateException;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayRejectedException;

@Component
public class TossPaymentResponseMapper {

    private static final Set<String> APPROVAL_REJECTION_CODES = Set.of(
            "REJECT_CARD_PAYMENT",
            "REJECT_ACCOUNT_PAYMENT",
            "NOT_FOUND_PAYMENT"
    );

    private static final Set<String> CANCELLATION_REJECTION_CODES = Set.of(
            "INVALID_REFUND_ACCOUNT_INFO",
            "EXCEED_CANCEL_AMOUNT_DISCOUNT_AMOUNT",
            "INVALID_REQUEST",
            "INVALID_REFUND_ACCOUNT_NUMBER",
            "INVALID_BANK",
            "NOT_MATCHES_REFUNDABLE_AMOUNT",
            "REFUND_REJECTED",
            "FORBIDDEN_BANK_REFUND_REQUEST",
            "FAILED_CLOSED_ACCOUNT_REFUND",
            "UNAUTHORIZED_KEY",
            "NOT_CANCELABLE_AMOUNT",
            "NOT_CANCELABLE_PAYMENT",
            "NOT_PARTIAL_CANCELABLE_PAYMENT",
            "NOT_CANCELABLE_PAYMENT_FOR_DORMANT_USER",
            "EXCEED_CANCEL_LIMIT",
            "INCORRECT_BASIC_AUTH_FORMAT",
            "NOT_FOUND_PAYMENT",
            "INVALID_IDEMPOTENCY_KEY",
            "INVALID_PAYMENT_KEY"
    );

    private static final Set<String> PROCESSING_CODES = Set.of(
            "IDEMPOTENT_REQUEST_PROCESSING",
            "ALREADY_PROCESSING_REQUEST",
            "ALREADY_REFUNDING_PAYMENT"
    );

    public TossPaymentResponse readApproval(
            HttpRequest httpRequest,
            ConvertibleClientHttpResponse response
    ) throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            TossPaymentResponse payment = readSuccess(
                    response,
                    ErrorCode.PAYMENT_CONFIRM_FAIL
            );
            validateApprovalResponse(payment);
            return payment;
        }

        TossError error = readError(
                response,
                ErrorCode.PAYMENT_CONFIRM_FAIL
        );

        if (PROCESSING_CODES.contains(error.code())) {
            throw new PaymentGatewayIndeterminateException(
                    ErrorCode.PAYMENT_CONFIRM_FAIL,
                    internalMessage(error)
            );
        }

        if (isDefinitiveRejection(error.code())) {
            throw new PaymentGatewayRejectedException(
                    ErrorCode.PAYMENT_CONFIRM_REJECTED
            );
        }

        throw new PaymentGatewayIndeterminateException(
                ErrorCode.PAYMENT_CONFIRM_FAIL,
                internalMessage(error)
        );
    }

    public TossPaymentResponse readCancellation(
            HttpRequest httpRequest,
            ConvertibleClientHttpResponse response
    ) throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            TossPaymentResponse payment = readSuccess(
                    response,
                    ErrorCode.PAYMENT_CANCEL_FAIL
            );
            validateCancellationResponse(payment);
            return payment;
        }

        TossError error = readError(
                response,
                ErrorCode.PAYMENT_CANCEL_FAIL
        );

        if (CANCELLATION_REJECTION_CODES.contains(error.code())) {
            throw new PaymentGatewayRejectedException(
                    ErrorCode.PAYMENT_CANCEL_REJECTED
            );
        }

        throw new PaymentGatewayIndeterminateException(
                ErrorCode.PAYMENT_CANCEL_FAIL,
                internalMessage(error)
        );
    }

    public TossPaymentResponse read(
            HttpRequest httpRequest,
            ConvertibleClientHttpResponse response
    ) throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            return readSuccess(
                    response,
                    ErrorCode.PAYMENT_READ_FAILED
            );
        }

        TossError error = readError(
                response,
                ErrorCode.PAYMENT_READ_FAILED
        );

        throw new PaymentGatewayIndeterminateException(
                ErrorCode.PAYMENT_READ_FAILED,
                internalMessage(error)
        );
    }

    private TossPaymentResponse readSuccess(
            ConvertibleClientHttpResponse response,
            ErrorCode fallbackErrorCode
    ) {
        try {
            TossPaymentResponse payment = response.bodyTo(TossPaymentResponse.class);
            if (payment == null) {
                throw new PaymentGatewayIndeterminateException(
                        fallbackErrorCode,
                        "Toss Payments returned an empty success response"
                );
            }
            return payment;
        } catch (RestClientException exception) {
            throw new PaymentGatewayIndeterminateException(
                    fallbackErrorCode,
                    exception.getMessage()
            );
        }
    }

    private TossError readError(
            ConvertibleClientHttpResponse response,
            ErrorCode fallbackErrorCode
    ) {
        try {
            TossError error = response.bodyTo(TossError.class);
            if (error == null || error.code() == null || error.code().isBlank()) {
                throw new PaymentGatewayIndeterminateException(
                        fallbackErrorCode,
                        "Toss Payments returned an invalid error response"
                );
            }
            return error;
        } catch (RestClientException exception) {
            throw new PaymentGatewayIndeterminateException(
                    fallbackErrorCode,
                    exception.getMessage()
            );
        }
    }

    private boolean isDefinitiveRejection(String code) {
        return APPROVAL_REJECTION_CODES.contains(code);
    }

    private void validateApprovalResponse(TossPaymentResponse payment) {
        if (payment.paymentKey() == null
                || payment.paymentKey().isBlank()
                || payment.approvedAt() == null
                || payment.totalAmount() == null) {
            throw new PaymentGatewayIndeterminateException(
                    ErrorCode.PAYMENT_CONFIRM_FAIL,
                    "Toss Payments returned an incomplete approval response"
            );
        }
    }

    private void validateCancellationResponse(TossPaymentResponse payment) {
        if (payment.paymentKey() == null
                || payment.paymentKey().isBlank()
                || payment.cancels() == null
                || payment.cancels().isEmpty()
                || payment.cancels().getLast().canceledAt() == null
                || payment.totalAmount() == null) {
            throw new PaymentGatewayIndeterminateException(
                    ErrorCode.PAYMENT_CANCEL_FAIL,
                    "Toss Payments returned an incomplete cancellation response"
            );
        }
    }

    private String internalMessage(TossError error) {
        return "Toss Payments error code=" + error.code();
    }

    record TossError(
            String code,
            String message
    ) {}
}
