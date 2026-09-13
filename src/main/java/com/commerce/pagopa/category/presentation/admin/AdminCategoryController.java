package com.commerce.pagopa.category.presentation.admin;

import com.commerce.pagopa.category.application.admin.CategoryAdminService;
import com.commerce.pagopa.category.application.admin.dto.request.AdminCategoryCreateRequestDto;
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

	private final CategoryAdminService categoryAdminService;

	@GetMapping
	public String list(
			@RequestParam(name = "categoryId", required = false) Long categoryId,
			Model model
	) {
		AdminCategoryPageResponseDto result = categoryAdminService.findPage(categoryId);

		model.addAttribute("categories", result.categories());
		model.addAttribute("rootCount", result.rootCount());
		model.addAttribute("detail", result.detail());

		return "admin/categories/list";
	}

	@GetMapping("/root")
	public String root(Model model) {
		model.addAttribute(
				"detail",
				categoryAdminService.findDetail(null)
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
				categoryAdminService.findDetail(categoryId)
		);

		return "admin/categories/fragments/detail :: detail";
	}

	@PostMapping
	public String create(
			@Valid @ModelAttribute AdminCategoryCreateRequestDto requestDto
	) {
		CategorySimpleResponseDto created = categoryAdminService.create(requestDto);

		return "redirect:/admin/categories?categoryId="
				+ created.categoryId();
	}
}
