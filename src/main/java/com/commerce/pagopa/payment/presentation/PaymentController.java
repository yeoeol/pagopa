package com.commerce.pagopa.payment.presentation;

import com.commerce.pagopa.payment.application.PaymentService;
import com.commerce.pagopa.payment.application.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.payment.application.dto.response.PaymentResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PAYMENT API", description = "결제 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 요청", description = "프론트엔드에서 결제 창을 띄우기 전, 결제 데이터를 준비하고 정보를 반환합니다.")
    @PostMapping("/ready/{id}")
    @PreAuthorize("@orderOwnerValidator.isOwner(#orderId, principal.userId)")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> readyPayment(
            @PathVariable("id") Long orderId
    ) {
        PaymentResponseDto response = paymentService.requestPayment(orderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "결제 요청", description = "프론트엔드에서 토스페이먼츠 결제창(SDK) 승인 성공 후, 백엔드에 최종 승인을 요청합니다.")
    @PostMapping("/confirm")
    @PreAuthorize("@paymentOwnerValidator.isOwner(#requestDto.orderId(), principal.userId)")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @Valid @RequestBody PaymentApproveRequestDto requestDto
    ) {
        paymentService.confirmPayment(requestDto);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
