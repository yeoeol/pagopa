package com.commerce.pagopa.seller.presentation.admin;

import com.commerce.pagopa.seller.application.admin.AdminSellerService;
import com.commerce.pagopa.seller.application.admin.dto.request.AdminSellerRejectRequestDto;
import com.commerce.pagopa.seller.application.admin.dto.response.AdminSellerRejectResponseDto;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/sellers")
public class AdminSellerController {

	private final AdminSellerService adminSellerService;

	@PostMapping("/{sellerId}/approve")
	public String approve(
			@PathVariable("sellerId") Long sellerId
	) {
		adminSellerService.approve(sellerId);
		return "redirect:/admin/users";
	}

	@PostMapping("/{sellerId}/reject")
	public String reject(
			@PathVariable("sellerId") Long sellerId,
			@Valid @ModelAttribute AdminSellerRejectRequestDto requestDto,
			Model model
	) {
		AdminSellerRejectResponseDto responseDto =
				adminSellerService.reject(sellerId, requestDto);

		model.addAttribute("rejectReason", responseDto.reason());
		return "redirect:/admin/users";
	}
}
