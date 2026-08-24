package com.commerce.pagopa.seller.presentation.admin;

import com.commerce.pagopa.seller.application.admin.AdminSellerService;
import com.commerce.pagopa.seller.application.admin.dto.response.AdminSellerPageResponseDto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/sellers")
public class AdminSellerController {

	private final AdminSellerService adminSellerService;

	@GetMapping("/requests")
	public String getPendingSellers(
			@PageableDefault(size = 10, page = 0) Pageable pageable,
			@RequestHeader(value = "HX-Request", required = false) String htmxRequest,
			HttpServletResponse response,
			Model model
	) {
		AdminSellerPageResponseDto pendingSellers =
				adminSellerService.getPendingSellers(pageable);

		model.addAttribute("sellers", pendingSellers);

		if (pendingSellers.totalPages() > 0
				&& pendingSellers.page() >= pendingSellers.totalPages()) {

			int lastPage = pendingSellers.totalPages() - 1;
			String redirectUrl = createRedirectUrl(
					lastPage,
					pendingSellers.size()
			);

			if ("true".equals(htmxRequest)) {
				response.setHeader("HX-Redirect", redirectUrl);
				return "admin/sellers/fragments/result :: result";
			}

			return "redirect:" + redirectUrl;
		}

		if ("true".equals(htmxRequest)) {
			return "admin/sellers/fragments/result :: result";
		}
		return "admin/sellers/requests";
	}

	@PostMapping("/{sellerId}/approve")
	public String approve(
			@PathVariable("sellerId") Long sellerId,
			@PageableDefault(page = 0, size = 10) Pageable pageable
	) {
		adminSellerService.approve(sellerId);
		return "redirect:/admin/sellers/requests?page=%d&size=%d"
				.formatted(pageable.getPageNumber(), pageable.getPageSize());
	}

	@PostMapping("/{sellerId}/reject")
	public String reject(
			@PathVariable("sellerId") Long sellerId,
			@PageableDefault(page = 0, size = 10) Pageable pageable
	) {
		adminSellerService.reject(sellerId);
		return "redirect:/admin/sellers/requests?page=%d&size=%d"
				.formatted(pageable.getPageNumber(), pageable.getPageSize());
	}

	private String createRedirectUrl(
			int page,
			int size
	) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromPath("/admin/sellers/requests")
				.queryParam("page", page)
				.queryParam("size", size);

		return builder.build()
				.encode()
				.toUriString();
	}
}
