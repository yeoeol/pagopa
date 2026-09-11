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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/request")
	public ResponseEntity<ApiResponse<PaymentResult>> request(
			@AuthenticationPrincipal(expression = "userId") Long userId,
			@Valid @RequestBody PaymentCommand command
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(paymentService.request(userId, command))
		);
	}

	@PostMapping("/approve")
	public ResponseEntity<ApiResponse<PaymentResult>> approve(
			@AuthenticationPrincipal(expression = "userId") Long userId,
			@Valid @RequestBody PaymentApprovalCommand command
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(paymentService.approve(userId, command))
		);
	}

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
