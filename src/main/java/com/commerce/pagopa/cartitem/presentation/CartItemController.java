package com.commerce.pagopa.cartitem.presentation;

import com.commerce.pagopa.cartitem.application.CartItemService;
import com.commerce.pagopa.cartitem.application.request.CartItemAddRequestDto;
import com.commerce.pagopa.cartitem.application.reseponse.CartItemResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CART-ITEM API", description = "장바구니 항목 개별 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart-items")
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
		CartItemResponseDto response = cartItemService.addCart(
				userId,
				requestDto
		);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(response));
	}

	@Operation(
			summary = "장바구니 항목 수량 증가",
			description = "장바구니 항목의 수량을 1 증가시킵니다."
	)
	@PostMapping("/{cartItemId}/increment")
	@PreAuthorize("@cartItemOwnerValidator.isOwner(#cartItemId, principal.userId)")
	public ResponseEntity<ApiResponse<CartItemResponseDto>> incrementQuantity(
			@PathVariable("cartItemId") Long cartItemId
	) {
		CartItemResponseDto response = cartItemService.incrementQuantity(cartItemId);
		return ResponseEntity.ok(
				ApiResponse.ok(response)
		);
	}

	@Operation(
			summary = "장바구니 항목 수량 감소",
			description = "장바구니 항목의 수량을 1 감소시킵니다. 수량이 0이 되면 해당 항목이 삭제되고 null을 반환합니다."
	)
	@PostMapping("/{cartItemId}/decrement")
	@PreAuthorize("@cartItemOwnerValidator.isOwner(#cartItemId, principal.userId)")
	public ResponseEntity<ApiResponse<CartItemResponseDto>> decrementQuantity(
			@PathVariable("cartItemId") Long cartItemId
	) {
		CartItemResponseDto response = cartItemService.decrementQuantity(cartItemId);
		if (response == null) {
			return ResponseEntity
					.status(HttpStatus.NO_CONTENT)
					.body(ApiResponse.ok(null));
		}
		return ResponseEntity.ok(
				ApiResponse.ok(response) // 수량이 0이 되어 삭제된 경우 response는 null
		);
	}

	@Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 특정 항목을 삭제합니다.")
	@DeleteMapping("/{cartItemId}")
	@PreAuthorize("@cartItemOwnerValidator.isOwner(#cartItemId, principal.userId)")
	public ResponseEntity<ApiResponse<Void>> deleteCart(
			@PathVariable("cartItemId") Long cartItemId
	) {
		cartItemService.delete(cartItemId);
		return ResponseEntity.ok(
				ApiResponse.ok()
		);
	}
}
