package com.commerce.pagopa.seller.presentation.admin;

import com.commerce.pagopa.seller.application.admin.AdminSellerService;
import com.commerce.pagopa.seller.application.admin.dto.request.AdminSellerRejectRequestDto;
import com.commerce.pagopa.seller.application.admin.dto.response.AdminSellerPageResponseDto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
			Model model
	) {
		AdminSellerPageResponseDto pendingSellers =
				adminSellerService.getPendingSellers(pageable);

		model.addAttribute("sellers", pendingSellers);

		if ("true".equals(htmxRequest)) {
			return "admin/sellers/fragments/result :: result";
		}

		return "admin/sellers/requests";
	}

	@PostMapping("/{sellerId}/approve")
	public String approve(
			@PathVariable("sellerId") Long sellerId
	) {
		adminSellerService.approve(sellerId);
		return "redirect:/admin/sellers/requests";
	}

	@PostMapping("/{sellerId}/reject")
	public String reject(
			@PathVariable("sellerId") Long sellerId,
			@Valid @ModelAttribute AdminSellerRejectRequestDto requestDto
	) {
		adminSellerService.reject(sellerId, requestDto);
		return "redirect:/admin/sellers/requests";
	}
}
