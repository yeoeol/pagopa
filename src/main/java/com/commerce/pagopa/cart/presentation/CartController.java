package com.commerce.pagopa.cart.presentation;

import com.commerce.pagopa.cart.application.CartService;
import com.commerce.pagopa.cart.application.dto.response.CartResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CART API", description = "장바구니 전체 단위 기능 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 전체 조회", description = "장바구니 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
		return ResponseEntity.ok(
                ApiResponse.ok(cartService.findUserCart(userId))
        );
    }

    @Operation(summary = "장바구니 비우기", description = "장바구니 품목들을 전체 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllCart(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        cartService.deleteAll(userId);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }
}
