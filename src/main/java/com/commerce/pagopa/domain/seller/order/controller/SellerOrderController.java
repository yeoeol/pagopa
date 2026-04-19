package com.commerce.pagopa.domain.seller.order.controller;

import com.commerce.pagopa.domain.seller.order.dto.request.OrderStatusChangeRequestDto;
import com.commerce.pagopa.domain.seller.order.dto.response.OrderResponseDto;
import com.commerce.pagopa.domain.seller.order.service.SellerOrderService;
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
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getSellerOrders(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerOrderService.findAll(userId, pageable))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@orderOwnerValidator.isOwner(#orderId, principal.userId)")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getSellerOrder(
            @PathVariable("id") Long orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerOrderService.find(orderId))
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@orderOwnerValidator.isOwner(#orderId, principal.userId)")
    public ResponseEntity<ApiResponse<OrderResponseDto>> changeStatus(
            @PathVariable("id") Long orderId,
            @Valid @RequestBody OrderStatusChangeRequestDto requestDto
    ) {
        sellerOrderService.changeStatus(orderId, requestDto);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
