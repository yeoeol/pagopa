package com.commerce.pagopa.order.presentation;

import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.application.dto.request.CartOrderRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderSearch;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ORDER API", description = "주문 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "장바구니 품목 주문 생성", description = "장바구니에서 선택된 품목들을 대상으로 주문을 생성합니다.")
    @PostMapping("/cart")
    public ResponseEntity<ApiResponse<OrderResponseDto>> orderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartOrderRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.orderFromCart(userDetails.getUserId(), requestDto)));
    }

    @Operation(summary = "바로 주문 생성", description = "장바구니를 거치지 않고 즉시 주문을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> order(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.order(userDetails.getUserId(), requestDto)));
    }

    @Operation(summary = "주문 상세 조회", description = "주문에 대한 정보를 상세 조회합니다.")
    @GetMapping("/{id}")
    @PreAuthorize("@orderOwnerValidator.isOwner(#orderId, principal.userId)")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(
            @PathVariable("id") Long orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.find(orderId))
        );
    }

    @Operation(summary = "주문 목록 조회", description = "검색 조건에 대해 주문 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject @ModelAttribute OrderSearch orderSearch,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.findAll(userDetails.getUserId(), orderSearch, pageable))
        );
    }

    @Operation(summary = "주문 전체 취소", description = "주문을 전체 취소합니다.")
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
