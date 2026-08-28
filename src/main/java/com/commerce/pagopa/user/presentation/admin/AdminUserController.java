package com.commerce.pagopa.user.presentation.admin;

import com.commerce.pagopa.user.application.admin.AdminUserService;
import com.commerce.pagopa.user.application.admin.dto.request.AdminUserSearchRequestDto;
import com.commerce.pagopa.user.application.admin.dto.response.AdminUserPageResponseDto;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public String users(
            @Valid @ModelAttribute AdminUserSearchRequestDto requestDto,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            HttpServletResponse response,
            Model model
    ) {
        AdminUserPageResponseDto result = adminUserService.search(requestDto);

        model.addAttribute("result", result);
        model.addAttribute("search", requestDto);

        if (result.totalPages() > 0
                && result.page() >= result.totalPages()) {

            int lastPage = result.totalPages() - 1;
            String redirectUrl = createRedirectUrl(
                    requestDto,
                    lastPage,
                    result.size()
            );

            if ("true".equals(htmxRequest)) {
                response.setHeader("HX-Redirect", redirectUrl);
                return "admin/users/fragments/result :: result";
            }

            return "redirect:" + redirectUrl;
        }

        if ("true".equals(htmxRequest)) {
            return "admin/users/fragments/result :: result";
        }
        return "admin/users/list";
    }

    @GetMapping("/{userId}")
    public String detail(
            @PathVariable("userId") Long userId,
            Model model
    ) {
        model.addAttribute("user", adminUserService.find(userId));
        return "admin/users/fragments/detail-modal :: detail";
    }

    @PostMapping("/{userId}/status/activate")
    public String activate(
            @PathVariable("userId") Long userId
    ) {
        adminUserService.activate(userId);
        return "redirect:/admin/users/" + userId;
    }

    @PostMapping("/{userId}/status/suspend")
    public String suspend(
            @PathVariable("userId") Long userId
    ) {
        adminUserService.suspend(userId);
        return "redirect:/admin/users/" + userId;
    }

    @PostMapping("/{userId}/status/ban")
    public String ban(
            @PathVariable("userId") Long userId
    ) {
        adminUserService.ban(userId);
        return "redirect:/admin/users/" + userId;
    }

    private String createRedirectUrl(
            AdminUserSearchRequestDto requestDto,
            int page,
            int size
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath("/admin/users")
                .queryParam("page", page)
                .queryParam("size", size);

        if (StringUtils.hasText(requestDto.keyword())) {
            builder.queryParam("keyword", requestDto.keyword().trim());
        }

        if (requestDto.status() != null) {
            builder.queryParam("status", requestDto.status().name());
        }

        if (requestDto.roleCode() != null) {
            builder.queryParam("roleCode", requestDto.roleCode().name());
        }

        return builder.build()
                .encode()
                .toUriString();
    }
}
