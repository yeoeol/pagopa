package com.commerce.pagopa.admin.order.presentation;

import com.commerce.pagopa.admin.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.admin.order.application.AdminOrderService;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getOrders(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminOrderService.findAll(pageable)));
    }
}
