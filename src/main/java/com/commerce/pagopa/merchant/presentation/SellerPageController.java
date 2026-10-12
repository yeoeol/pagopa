package com.commerce.pagopa.merchant.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerPageController {

    @GetMapping
    public String index() {
        return "redirect:/seller/products";
    }
}
