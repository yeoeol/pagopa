package com.commerce.pagopa.payment.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.payment.application.PaymentService;
import com.commerce.pagopa.payment.application.dto.request.CancelPaymentCommand;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalCommand;
import com.commerce.pagopa.payment.application.dto.request.PaymentCommand;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PAYMENT API", description = "결제 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final PaymentService paymentService;

	@Operation(summary = "결제 요청", description = "특정 주문에 대해 결제 수단과 함께 결제를 요청합니다.")
	@PostMapping("/request")
	public ResponseEntity<ApiResponse<PaymentResult>> request(
			@AuthenticationPrincipal(expression = "userId") Long userId,
			@Valid @RequestBody PaymentCommand command
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(paymentService.request(userId, command))
		);
	}

	@Operation(summary = "결제 승인", description = "요청된 결제에 대해 결제를 승인합니다.")
	@PostMapping("/approve")
	public ResponseEntity<ApiResponse<PaymentResult>> approve(
			@AuthenticationPrincipal(expression = "userId") Long userId,
			@Valid @RequestBody PaymentApprovalCommand command
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(paymentService.approve(userId, command))
		);
	}

	@Operation(summary = "결제 취소", description = "결제에 대해 취소를 요청합니다.")
	@PostMapping("/cancel")
	public ResponseEntity<ApiResponse<PaymentResult>> cancel(
			@AuthenticationPrincipal(expression = "userId") Long userId,
			@Valid @RequestBody CancelPaymentCommand command
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(paymentService.cancel(userId, command))
		);
	}
}
