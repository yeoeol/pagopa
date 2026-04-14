package com.commerce.pagopa.domain.payment.controller;

import com.commerce.pagopa.domain.payment.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.domain.payment.dto.response.PaymentResponseDto;
import com.commerce.pagopa.domain.payment.service.PaymentService;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 프론트엔드에서 결제 창을 띄우기 전, 결제 데이터를 준비하고 정보를 받아가는 API
     */
    @PostMapping("/ready/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> readyPayment(
            @PathVariable("id") Long orderId,
            @RequestParam String orderName,
            @RequestParam String customerName
    ) {
        PaymentResponseDto response = paymentService.requestPayment(orderId, orderName, customerName);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 프론트엔드에서 토스페이먼츠 결제창(SDK) 승인 성공 후, 백엔드에 최종 승인을 요청하는 API
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @Valid @RequestBody PaymentApproveRequestDto requestDto
    ) {
        paymentService.confirmPayment(requestDto);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
