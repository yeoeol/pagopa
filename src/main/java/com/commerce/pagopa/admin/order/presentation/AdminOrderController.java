package com.commerce.pagopa.admin.order.presentation;

import com.commerce.pagopa.admin.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.admin.order.application.AdminOrderService;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ADMIN ORDER API", description = "관리자 - 주문 관리 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "주문 전체 조회", description = "전체 주문을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getOrders(
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminOrderService.findAll(pageable)));
    }
}
