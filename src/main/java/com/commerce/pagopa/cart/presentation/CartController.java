package com.commerce.pagopa.cart.presentation;

import com.commerce.pagopa.cart.application.dto.request.CartAddRequestDto;
import com.commerce.pagopa.cart.application.dto.response.CartResponseDto;
import com.commerce.pagopa.cart.application.CartService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CART API", description = "장바구니 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "장바구니 추가",
            description = "장바구니에 상품을 추가합니다. 이미 담긴 상품이면 요청 수량만큼 누적됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CartResponseDto>> addCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartAddRequestDto requestDto
    ) {
        CartResponseDto response = cartService.addCart(userDetails.getUserId(), requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @Operation(summary = "장바구니 조회", description = "장바구니 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartResponseDto>>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<CartResponseDto> responses = cartService.findUserCart(userDetails.getUserId());
        return ResponseEntity.ok(
                ApiResponse.ok(responses)
        );
    }

    @Operation(
            summary = "장바구니 품목 수량 증가",
            description = "장바구니 품목의 수량을 1 증가시킵니다."
    )
    @PostMapping("/{id}/increment")
    @PreAuthorize("@cartOwnerValidator.isOwner(#cartId, principal.userId)")
    public ResponseEntity<ApiResponse<CartResponseDto>> incrementQuantity(
            @PathVariable("id") Long cartId
    ) {
        CartResponseDto response = cartService.incrementQuantity(cartId);
        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @Operation(
            summary = "장바구니 품목 수량 감소",
            description = "장바구니 품목의 수량을 1 감소시킵니다. 수량이 0이 되면 해당 품목이 삭제되고 null을 반환합니다."
    )
    @PostMapping("/{id}/decrement")
    @PreAuthorize("@cartOwnerValidator.isOwner(#cartId, principal.userId)")
    public ResponseEntity<ApiResponse<CartResponseDto>> decrementQuantity(
            @PathVariable("id") Long cartId
    ) {
        CartResponseDto response = cartService.decrementQuantity(cartId);
        return ResponseEntity.ok(
                ApiResponse.ok(response) // 수량이 0이 되어 삭제된 경우 response는 null
        );
    }

    @Operation(summary = "장바구니 품목 삭제", description = "장바구니에서 특정 품목을 삭제합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("@cartOwnerValidator.isOwner(#cartId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> deleteCart(
            @PathVariable("id") Long cartId
    ) {
        cartService.delete(cartId);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    @Operation(summary = "장바구니 비우기", description = "장바구니 품목들을 전체 삭제합니다.")
    @DeleteMapping
    @PreAuthorize("@cartOwnerValidator.isOwner(#cartId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> deleteAllCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        cartService.deleteAll(userDetails.getUserId());
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }
}
