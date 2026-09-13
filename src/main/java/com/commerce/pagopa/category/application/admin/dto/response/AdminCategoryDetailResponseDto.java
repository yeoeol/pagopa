package com.commerce.pagopa.category.application.admin.dto.response;

import java.util.List;

public record AdminCategoryDetailResponseDto(
		Long categoryId,
		String name,
		int depth,
		String path,
		String parentName,
		List<AdminCategoryTreeItemResponseDto> breadcrumbs,
		List<AdminCategoryTreeItemResponseDto> children
) {
	public static AdminCategoryDetailResponseDto of(
			Long categoryId,
			String name,
			int depth,
			String path,
			String parentName,
			List<AdminCategoryTreeItemResponseDto> breadcrumbs,
			List<AdminCategoryTreeItemResponseDto> children
	) {
		return new AdminCategoryDetailResponseDto(
				categoryId,
				name,
				depth,
				path,
				parentName,
				breadcrumbs,
				children
		);
	}
}
