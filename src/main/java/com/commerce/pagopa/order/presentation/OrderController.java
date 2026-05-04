package com.commerce.pagopa.order.presentation;

import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.application.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/cart")
    public ResponseEntity<ApiResponse<OrderResponseDto>> orderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartOrderRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.orderFromCart(userDetails.getUserId(), requestDto)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> order(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.order(userDetails.getUserId(), requestDto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@orderOwnerValidator.isOwner(#orderId, principal.userId)")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(
            @PathVariable("id") Long orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.find(orderId))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute OrderSearch orderSearch,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.findAll(userDetails.getUserId(), orderSearch, pageable))
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("@orderOwnerValidator.isOwner(#orderId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable("id") Long orderId,
            @Valid @RequestBody OrderCancelRequestDto requestDto
    ) {
        orderService.cancelOrder(orderId, requestDto);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
