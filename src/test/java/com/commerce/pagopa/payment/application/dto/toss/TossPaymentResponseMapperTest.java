package com.commerce.pagopa.payment.application.dto.toss;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayIndeterminateException;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayRejectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class TossPaymentResponseMapperTest {

    private TossPaymentResponseMapper mapper;

    @Mock
    private ConvertibleClientHttpResponse response;

    @BeforeEach
    void setUp() {
        mapper = new TossPaymentResponseMapper();
    }

    @Test
    void read_approval_returns_payment_for_complete_success_response() throws IOException {
        TossPaymentResponse payment = approvalResponse();
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.bodyTo(TossPaymentResponse.class)).thenReturn(payment);

        TossPaymentResponse result = mapper.readApproval(
                null,
                response,
                new PaymentApprovalRequest(
                        null,
                        null,
                        payment.paymentKey(),
                        payment.orderId(),
                        payment.totalAmount(),
                        payment.method(),
                        null
                )
        );

        assertThat(result).isSameAs(payment);
    }

    @Test
    void read_approval_rejects_definitive_provider_failure() throws IOException {
        stubErrorResponse(
                HttpStatus.FORBIDDEN,
                "REJECT_CARD_PAYMENT"
        );

        PaymentGatewayRejectedException exception = catchThrowableOfType(
                PaymentGatewayRejectedException.class,
                () -> mapper.readApproval(
                        null,
                        response,
                        null
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_CONFIRM_REJECTED);
    }

    @Test
    void read_approval_keeps_intermediate_state_while_idempotentRequest_is_processing() throws IOException {
        stubErrorResponse(
                HttpStatus.CONFLICT,
                "IDEMPOTENT_REQUEST_PROCESSING"
        );

        PaymentGatewayIndeterminateException exception = catchThrowableOfType(
                PaymentGatewayIndeterminateException.class,
                () -> mapper.readApproval(
                        null,
                        response,
                        null
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_CONFIRM_FAIL);
    }

    @Test
    void read_approval_keeps_intermediate_state_for_unknown_error() throws IOException {
        stubErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "NEW_UNKNOWN_ERROR"
        );

        PaymentGatewayIndeterminateException exception = catchThrowableOfType(
                PaymentGatewayIndeterminateException.class,
                () -> mapper.readApproval(
                        null,
                        response,
                        null
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_CONFIRM_FAIL);
    }

    @Test
    void read_cancellation_returns_payment_with_cancellation_history() throws IOException {
        TossPaymentResponse payment = cancellationResponse();
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.bodyTo(TossPaymentResponse.class)).thenReturn(payment);

        TossPaymentResponse result = mapper.readCancellation(
                null,
                response
        );

        assertThat(result).isSameAs(payment);
        assertThat(result.cancels()).hasSize(1);
    }

    @Test
    void read_cancellation_deserializes_actual_tossPayment_body() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.tosspayments.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder)
                .build();
        RestClient restClient = builder.build();

        server.expect(requestTo("https://api.tosspayments.com/v1/payments/payment-key"))
                .andRespond(withSuccess(
                        """
                                {
                                  "mId": "tosspayments",
                                  "paymentKey": "payment-key",
                                  "orderId": "order-id",
                                  "orderName": "order-name",
                                  "status": "CANCELED",
                                  "requestedAt": "2026-10-23T10:00:00+09:00",
                                  "approvedAt": "2026-10-23T10:01:00+09:00",
                                  "cancels": [
                                    {
                                      "transactionKey": "transaction-key",
                                      "cancelReason": "customer request",
                                      "canceledAt": "2026-10-23T10:02:00+09:00",
                                      "cancelAmount": 10000,
                                      "cancelStatus": "DONE"
                                    }
                                  ],
                                  "totalAmount": 10000,
                                  "balanceAmount": 0,
                                  "method": "CARD"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        TossPaymentResponse result = restClient.get()
                .uri("/v1/payments/payment-key")
                .exchange(mapper::readCancellation);

        assertThat(result.cancels()).hasSize(1);
        assertThat(result.cancels().getFirst().canceledAt())
                .isEqualTo(OffsetDateTime.parse("2026-10-23T10:02:00+09:00"));
        server.verify();
    }

    @Test
    void read_cancellation_rejects_invalid_cancellation_amount() throws IOException {
        stubErrorResponse(
                HttpStatus.FORBIDDEN,
                "NOT_CANCELABLE_AMOUNT"
        );

        PaymentGatewayRejectedException exception = catchThrowableOfType(
                PaymentGatewayRejectedException.class,
                () -> mapper.readCancellation(
                        null,
                        response
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_REJECTED);
    }

    @Test
    void read_cancellation_keeps_intermediate_state_when_provider_says_already_canceled() throws IOException {
        stubErrorResponse(
                HttpStatus.BAD_REQUEST,
                "ALREADY_CANCELED_PAYMENT"
        );

        PaymentGatewayIndeterminateException exception = catchThrowableOfType(
                PaymentGatewayIndeterminateException.class,
                () -> mapper.readCancellation(
                        null,
                        response
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_FAIL);
    }

    @Test
    void read_cancellation_rejects_incomplete_success_response() throws IOException {
        TossPaymentResponse payment = new TossPaymentResponse(
                "payment-key",
                "order-id",
                "order-name",
                "CANCELED",
                OffsetDateTime.parse("2026-10-23T10:00:00+09:00"),
                OffsetDateTime.parse("2026-10-23T10:01:00+09:00"),
                List.of(),
                10_000,
                0,
                "CARD"
        );
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.bodyTo(TossPaymentResponse.class)).thenReturn(payment);

        PaymentGatewayIndeterminateException exception = catchThrowableOfType(
                PaymentGatewayIndeterminateException.class,
                () -> mapper.readCancellation(
                        null,
                        response
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_CANCEL_FAIL);
    }

    private void stubErrorResponse(HttpStatus status, String code) throws IOException {
        when(response.getStatusCode()).thenReturn(status);
        when(response.bodyTo(TossPaymentResponseMapper.TossError.class)).thenReturn(
                new TossPaymentResponseMapper.TossError(
                        code,
                        "provider error"
                )
        );
    }

    private TossPaymentResponse approvalResponse() {
        return new TossPaymentResponse(
                "payment-key",
                "order-id",
                "order-name",
                "DONE",
                OffsetDateTime.parse("2026-10-23T10:00:00+09:00"),
                OffsetDateTime.parse("2026-10-23T10:01:00+09:00"),
                null,
                10_000,
                10_000,
                "CARD"
        );
    }

    private TossPaymentResponse cancellationResponse() {
        TossPaymentResponse.Cancel cancellation = new TossPaymentResponse.Cancel(
                "transaction-key",
                "customer request",
                OffsetDateTime.parse("2026-10-23T10:02:00+09:00"),
                10_000,
                "DONE"
        );

        return new TossPaymentResponse(
                "payment-key",
                "order-id",
                "order-name",
                "CANCELED",
                OffsetDateTime.parse("2026-10-23T10:00:00+09:00"),
                OffsetDateTime.parse("2026-10-23T10:01:00+09:00"),
                List.of(cancellation),
                10_000,
                0,
                "CARD"
        );
    }
}
