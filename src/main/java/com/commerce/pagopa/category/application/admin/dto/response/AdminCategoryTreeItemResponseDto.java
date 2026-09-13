package com.commerce.pagopa.category.application.admin.dto.response;

public record AdminCategoryTreeItemResponseDto(
		Long categoryId,
		Long parentId,
		String name,
		int depth,
		String path
) {
	public static AdminCategoryTreeItemResponseDto of(
			Long categoryId,
			Long parentId,
			String name,
			int depth,
			String path
	) {
		return new AdminCategoryTreeItemResponseDto(
				categoryId,
				parentId,
				name,
				depth,
				path
		);
	}
}
