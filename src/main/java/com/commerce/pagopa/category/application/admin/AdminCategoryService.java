package com.commerce.pagopa.category.application.admin;

import com.commerce.pagopa.category.application.admin.dto.request.AdminCategoryCreateRequestDto;
import com.commerce.pagopa.category.application.admin.dto.response.AdminCategoryDetailResponseDto;
import com.commerce.pagopa.category.application.admin.dto.response.AdminCategoryPageResponseDto;
import com.commerce.pagopa.category.application.admin.dto.response.AdminCategoryTreeItemResponseDto;
import com.commerce.pagopa.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import lombok.RequiredArgsConstructor;

import static com.commerce.pagopa.global.util.StringUtil.normalize;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

	private final CategoryRepository categoryRepository;

	private static final Comparator<Category> CATEGORY_ORDER =
			Comparator.comparing(Category::getName)
					.thenComparing(Category::getId);

	@Transactional
	public CategorySimpleResponseDto create(AdminCategoryCreateRequestDto requestDto) {
		String normalizedName = normalize(requestDto.name());

		Category category = requestDto.parentId() == null
				? Category.createRoot(normalizedName)
				: categoryRepository.findByIdOrThrow(requestDto.parentId())
						.addChild(normalizedName);

		return CategorySimpleResponseDto.from(
				categoryRepository.save(category)
		);
	}

	@Transactional(readOnly = true)
	public AdminCategoryPageResponseDto findPage(Long categoryId) {
		List<AdminCategoryTreeItemResponseDto> categories =
				buildTreeItems(categoryRepository.findAll());

		int rootCount = (int) categories.stream()
				.filter(category -> category.parentId() == null)
				.count();

		AdminCategoryDetailResponseDto detail =
				buildDetail(categoryId, categories);

		return AdminCategoryPageResponseDto.of(
				categories,
				rootCount,
				detail
		);
	}

	@Transactional(readOnly = true)
	public AdminCategoryDetailResponseDto findDetail(Long categoryId) {
		List<AdminCategoryTreeItemResponseDto> categories =
				buildTreeItems(categoryRepository.findAll());

		return buildDetail(categoryId, categories);
	}

	private List<AdminCategoryTreeItemResponseDto> buildTreeItems(
			List<Category> categories
	) {
		Map<Long, List<Category>> childrenByParentId = new HashMap<>();

		for (Category category : categories) {
			Long parentId = category.getParent() == null
					? null
					: category.getParent().getId();

			childrenByParentId
					.computeIfAbsent(parentId, ignored -> new ArrayList<>())
					.add(category);
		}

		childrenByParentId.values()
				.forEach(children -> children.sort(CATEGORY_ORDER));

		List<AdminCategoryTreeItemResponseDto> result = new ArrayList<>();

		appendTreeItems(
				null,
				0,
				null,
				childrenByParentId,
				result
		);

		return List.copyOf(result);
	}

	private void appendTreeItems(
			Long parentId,
			int depth,
			String parentPath,
			Map<Long, List<Category>> childrenByParentId,
			List<AdminCategoryTreeItemResponseDto> result
	) {
		List<Category> children =
				childrenByParentId.getOrDefault(parentId, List.of());

		for (Category category : children) {
			String path = parentPath == null
					? category.getName()
					: parentPath + " > " + category.getName();

			result.add(AdminCategoryTreeItemResponseDto.of(
					category.getId(),
					parentId,
					category.getName(),
					depth,
					path
			));

			appendTreeItems(
					category.getId(),
					depth + 1,
					path,
					childrenByParentId,
					result
			);
		}
	}

	private AdminCategoryDetailResponseDto buildDetail(
			Long categoryId,
			List<AdminCategoryTreeItemResponseDto> categories
	) {
		if (categoryId == null) {
			List<AdminCategoryTreeItemResponseDto> rootCategories =
					categories.stream()
							.filter(category -> category.parentId() == null)
							.toList();

			return AdminCategoryDetailResponseDto.of(
					null,
					null,
					-1,
					null,
					null,
					List.of(),
					rootCategories
			);
		}

		Map<Long, AdminCategoryTreeItemResponseDto> categoryById =
				new HashMap<>();

		for (AdminCategoryTreeItemResponseDto category : categories) {
			categoryById.put(category.categoryId(), category);
		}

		AdminCategoryTreeItemResponseDto selected =
				categoryById.get(categoryId);

		if (selected == null) {
			throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
		}

		List<AdminCategoryTreeItemResponseDto> breadcrumbs =
				buildBreadcrumbs(selected, categoryById);

		List<AdminCategoryTreeItemResponseDto> children =
				categories.stream()
						.filter(category ->
										categoryId.equals(category.parentId())
						).toList();

		AdminCategoryTreeItemResponseDto parent =
				selected.parentId() == null
						? null
						: categoryById.get(selected.parentId());

		return AdminCategoryDetailResponseDto.of(
				selected.categoryId(),
				selected.name(),
				selected.depth(),
				selected.path(),
				parent == null ? null : parent.name(),
				breadcrumbs,
				children
		);
	}

	private List<AdminCategoryTreeItemResponseDto> buildBreadcrumbs(
			AdminCategoryTreeItemResponseDto selected,
			Map<Long, AdminCategoryTreeItemResponseDto> categoryById
	) {
		List<AdminCategoryTreeItemResponseDto> breadcrumbs =
				new ArrayList<>();

		AdminCategoryTreeItemResponseDto current = selected;

		while (current != null) {
			breadcrumbs.add(current);

			current = current.parentId() == null
					? null
					: categoryById.get(current.parentId());
		}

		Collections.reverse(breadcrumbs);

		return List.copyOf(breadcrumbs);
	}
}
