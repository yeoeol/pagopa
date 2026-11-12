package com.commerce.pagopa.basket.presentation;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.basket.application.CartItemService;
import com.commerce.pagopa.basket.application.dto.request.CartItemAddRequestDto;
import com.commerce.pagopa.basket.application.dto.response.CartItemResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;

@Tag(
        name = "CART-ITEM API",
        description = "장바구니 항목 개별 관리 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart/items")
public class CartItemController {

    private final CartItemService cartItemService;

    @Operation(
            summary = "장바구니 항목 추가",
            description = "장바구니에 상품을 추가합니다. 이미 담긴 상품이면 요청 수량만큼 누적됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponseDto>> addCartItem(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody CartItemAddRequestDto requestDto
    ) {
        CartItemResponseDto response = cartItemService.addCartItem(
                userId,
                requestDto
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @Operation(
            summary = "장바구니 항목 수량 증가",
            description = "장바구니 항목의 수량을 1 증가시킵니다."
    )
    @PostMapping("/{productId}/increment")
    public ResponseEntity<ApiResponse<CartItemResponseDto>> incrementQuantity(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("productId") Long productId
    ) {
        CartItemResponseDto response = cartItemService.incrementQuantity(
                userId,
                productId
        );
        return ResponseEntity.ok(
                ApiResponse.ok(
                        response
                )
        );
    }

    @Operation(
            summary = "장바구니 항목 수량 감소",
            description = "장바구니 항목의 수량을 1 감소시킵니다. 수량이 0이 되면 해당 항목이 삭제되고 null을 반환합니다."
    )
    @PostMapping("/{productId}/decrement")
    public ResponseEntity<ApiResponse<CartItemResponseDto>> decrementQuantity(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("productId") Long productId
    ) {
        CartItemResponseDto response = cartItemService.decrementQuantity(
                userId,
                productId
        );
        if (response.cartQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .build();
        }
        return ResponseEntity.ok(
                ApiResponse.ok(response) // 수량이 0이 되어 삭제된 경우 response는 null
        );
    }

    @Operation(
            summary = "장바구니 항목 삭제",
            description = "장바구니에서 특정 항목을 삭제합니다."
    )
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteCart(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("productId") Long productId
    ) {
        cartItemService.delete(userId, productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
