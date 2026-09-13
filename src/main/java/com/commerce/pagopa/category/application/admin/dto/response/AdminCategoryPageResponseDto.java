package com.commerce.pagopa.category.application.admin.dto.response;

import java.util.List;

public record AdminCategoryPageResponseDto(
		List<AdminCategoryTreeItemResponseDto> categories,
		int rootCount,
		AdminCategoryDetailResponseDto detail
) {
	public static AdminCategoryPageResponseDto of(
			List<AdminCategoryTreeItemResponseDto> categories,
			int rootCount,
			AdminCategoryDetailResponseDto detail
	) {
		return new AdminCategoryPageResponseDto(
				categories,
				rootCount,
				detail
		);
	}
}
