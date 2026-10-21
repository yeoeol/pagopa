package com.commerce.pagopa.merchant.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
public class SellerLoginPageController {

    @GetMapping("/login")
    public String login() {
        return "seller/login";
    }
}
