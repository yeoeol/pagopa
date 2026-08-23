package com.commerce.pagopa.admin.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String index() {
        return "redirect:/admin/users";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "admin/access-denied";
    }
}
