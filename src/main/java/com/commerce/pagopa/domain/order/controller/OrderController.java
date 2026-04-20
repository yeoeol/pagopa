package com.commerce.pagopa.domain.order.controller;

import com.commerce.pagopa.domain.order.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.domain.order.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.domain.order.dto.request.OrderSearch;
import com.commerce.pagopa.domain.order.dto.response.OrderResponseDto;
import com.commerce.pagopa.domain.order.service.OrderService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import io.micrometer.core.annotation.Counted;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Counted("my.order")
    @PostMapping("/cart")
    public ResponseEntity<ApiResponse<OrderResponseDto>> orderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartOrderRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.orderFromCart(userDetails.getUserId(), requestDto)));
    }

    @Counted("my.order")
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
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute OrderSearch orderSearch
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.findAll(userDetails.getUserId(), orderSearch))
        );
    }

    @Counted("my.order")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("@orderOwnerValidator.isOwner(#orderId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable("id") Long orderId
    ) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
