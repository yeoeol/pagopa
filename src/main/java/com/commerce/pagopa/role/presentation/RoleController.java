package com.commerce.pagopa.role.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.role.application.RoleService;
import com.commerce.pagopa.role.application.response.RoleResponseDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ROLE API", description = "역할(권한) 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class RoleController {

	private final RoleService roleService;

	@Operation(summary = "역할(권한) 목록 조회", description = "역할(권한) 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<List<RoleResponseDto>>> getAll(
			@RequestParam(required = false) Boolean enabled
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(roleService.findAll(enabled))
		);
	}

	@Operation(summary = "역할(권한) 상세 조회", description = "특정 역할(권한)을 조회합니다.")
	@GetMapping("/{roleId}")
	public ResponseEntity<ApiResponse<RoleResponseDto>> getDetail(
			@PathVariable("roleId") Long roleId
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(roleService.find(roleId))
		);
	}

	@Operation(summary = "역할(권한) 비활성화", description = "역할(권한)을 비활성화합니다.")
	@PatchMapping("/{roleId}")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable("roleId") Long roleId
	) {
		roleService.inactive(roleId);
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
