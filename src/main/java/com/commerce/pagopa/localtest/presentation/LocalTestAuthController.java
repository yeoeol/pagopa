package com.commerce.pagopa.localtest.presentation;

import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.localtest.application.LocalTestAuthService;
import com.commerce.pagopa.localtest.application.dto.request.LocalTestTokenRequestDto;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@Profile("load-test")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/local-test/auth")
public class LocalTestAuthController {

	private final LocalTestAuthService localTestAuthService;

	@PostMapping("/token")
	public ResponseEntity<ApiResponse<TokenResponseDto>> issueToken(
			@RequestBody LocalTestTokenRequestDto request
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(localTestAuthService.issueToken(request.userKey()))
		);
	}
}
