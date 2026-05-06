package com.commerce.pagopa.seller.order.presentation;

import com.commerce.pagopa.seller.order.application.SellerOrderService;
import com.commerce.pagopa.seller.order.application.dto.request.OrderStatusChangeRequestDto;
import com.commerce.pagopa.seller.order.application.dto.response.SellerOrderResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SELLER ORDER API", description = "판매자 주문 관리 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@RequestMapping("/api/v1/seller/orders")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    @Operation(summary = "판매자 상품으로 들어온 주문 목록 조회", description = "판매자 상품으로 들어온 주문 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SellerOrderResponseDto>>> getSellerOrders(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @ParameterObject @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerOrderService.findAll(userId, pageable))
        );
    }

    @Operation(summary = "판매자 상품으로 들어온 주문 상세 조회", description = "판매자 상품으로 들어온 특정 주문을 조회합니다.")
    @GetMapping("/{sellerOrderId}")
    public ResponseEntity<ApiResponse<SellerOrderResponseDto>> getSellerOrder(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("sellerOrderId") Long sellerOrderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerOrderService.find(sellerOrderId, userId))
        );
    }

    @Operation(summary = "주문 상태 변경", description = "주문 상태를 변경합니다.")
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
