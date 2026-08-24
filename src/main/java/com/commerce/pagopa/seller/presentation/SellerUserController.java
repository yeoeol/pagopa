package com.commerce.pagopa.seller.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.seller.application.SellerUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "SELLER USER API", description = "판매자 - 회원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sellers")
public class SellerUserController {

	private final SellerUserService sellerUserService;

	@Operation(summary = "판매자 승급 요청", description = "일반 회원이 판매자 권한을 요청합니다.")
	@PostMapping("/request")
	public ResponseEntity<ApiResponse<Void>> requestSellerRole(
			@AuthenticationPrincipal(expression = "userId") Long userId
	) {
		sellerUserService.request(userId);
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
