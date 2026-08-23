package com.commerce.pagopa.user.presentation.admin;

import com.commerce.pagopa.user.application.admin.AdminUserService;
import com.commerce.pagopa.user.application.admin.dto.request.AdminUserSearchRequestDto;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            Model model
    ) {
        model.addAttribute("result", adminUserService.search(requestDto));
        model.addAttribute("search", requestDto);

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
}
