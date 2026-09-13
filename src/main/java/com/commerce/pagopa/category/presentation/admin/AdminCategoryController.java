package com.commerce.pagopa.category.presentation.admin;

import com.commerce.pagopa.category.application.admin.AdminCategoryService;
import com.commerce.pagopa.category.application.admin.dto.request.AdminCategoryCreateRequestDto;
import com.commerce.pagopa.category.application.admin.dto.request.AdminCategoryUpdateRequestDto;
import com.commerce.pagopa.category.application.admin.dto.response.AdminCategoryPageResponseDto;
import com.commerce.pagopa.category.application.dto.response.CategorySimpleResponseDto;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

	private final AdminCategoryService adminCategoryService;

	@GetMapping
	public String list(
			@RequestParam(name = "categoryId", required = false) Long categoryId,
			Model model
	) {
		AdminCategoryPageResponseDto result = adminCategoryService.findPage(categoryId);

		model.addAttribute("categories", result.categories());
		model.addAttribute("rootCount", result.rootCount());
		model.addAttribute("detail", result.detail());

		return "admin/categories/list";
	}

	@GetMapping("/root")
	public String root(Model model) {
		model.addAttribute(
				"detail",
				adminCategoryService.findDetail(null)
		);

		return "admin/categories/fragments/detail :: detail";
	}

	@GetMapping("/{categoryId}")
	public String detail(
			@PathVariable("categoryId") Long categoryId,
			Model model
	) {
		model.addAttribute(
				"detail",
				adminCategoryService.findDetail(categoryId)
		);

		return "admin/categories/fragments/detail :: detail";
	}

	@PostMapping
	public String create(
			@Valid @ModelAttribute AdminCategoryCreateRequestDto requestDto
	) {
		CategorySimpleResponseDto created = adminCategoryService.create(requestDto);

		return "redirect:/admin/categories?categoryId="
				+ created.categoryId();
	}

	@PostMapping("/{categoryId}/rename")
	public String update(
			@PathVariable("categoryId") Long categoryId,
			@Valid @ModelAttribute AdminCategoryUpdateRequestDto requestDto
	) {
		adminCategoryService.update(categoryId, requestDto);
		return "redirect:/admin/categories?categoryId="
				+ categoryId;
	}
}
