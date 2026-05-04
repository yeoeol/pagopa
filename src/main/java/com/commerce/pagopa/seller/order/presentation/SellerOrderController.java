package com.commerce.pagopa.seller.order.presentation;

import com.commerce.pagopa.seller.order.application.SellerOrderService;
import com.commerce.pagopa.seller.order.application.dto.request.OrderStatusChangeRequestDto;
import com.commerce.pagopa.seller.order.application.dto.response.SellerOrderResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@RequestMapping("/api/v1/seller/orders")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SellerOrderResponseDto>>> getSellerOrders(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerOrderService.findAll(userId, pageable))
        );
    }

    @GetMapping("/{sellerOrderId}")
    public ResponseEntity<ApiResponse<SellerOrderResponseDto>> getSellerOrder(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("sellerOrderId") Long sellerOrderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerOrderService.find(sellerOrderId, userId))
        );
    }

    @PatchMapping("/{sellerOrderId}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("sellerOrderId") Long sellerOrderId,
            @Valid @RequestBody OrderStatusChangeRequestDto requestDto
    ) {
        sellerOrderService.changeStatus(sellerOrderId, userId, requestDto);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
