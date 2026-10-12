package com.commerce.pagopa.merchant.presentation;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.merchant.application.SellerProductService;
import com.commerce.pagopa.merchant.application.dto.product.request.ProductAddStockRequestDto;
import com.commerce.pagopa.merchant.application.dto.product.request.ProductRegisterRequestDto;
import com.commerce.pagopa.merchant.application.dto.seller.response.SellerProductPageResponseDto;

@Controller
@RequiredArgsConstructor
@RequestMapping("/seller/products")
@PreAuthorize("hasRole('SELLER')")
public class SellerProductPageController {

    private final SellerProductService sellerProductService;

    @GetMapping
    public String list(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PageableDefault(
                    size = 10
            ) Pageable pageable,
            @RequestParam(
                    name = "notice",
                    required = false
            ) String notice,
            @RequestHeader(
                    name = "HX-Request",
                    defaultValue = "false"
            ) boolean htmxRequest,
            HttpServletResponse response,
            Model model
    ) {
        SellerProductPageResponseDto result = sellerProductService.findAll(
                userId,
                pageable
        );

        model.addAttribute(
                "result",
                result
        );
        model.addAttribute(
                "notice",
                notice
        );

        if (isOutOfRange(result)) {
            int lastPage = result.totalPages() - 1;
            String redirectUrl = createRedirectUrl(
                    lastPage,
                    result.size()
            );

            if (htmxRequest) {
                response.setHeader(
                        "HX-Redirect",
                        redirectUrl
                );
                return "seller/products/fragments/result :: result";
            }
            return "redirect:" + redirectUrl;
        }

        if (htmxRequest) {
            return "seller/products/fragments/result :: result";
        }

        model.addAttribute(
                "registerForm",
                ProductRegisterRequestDto.emptyForm()
        );
        model.addAttribute(
                "categoryOptions",
                sellerProductService.findCategoryOptions()
        );

        return "seller/products/list";
    }

    @GetMapping("/{productId}")
    @PreAuthorize(
        "@sellerProductOwnerValidator.isOwner(#productId, principal.userId)"
    )
    public String detail(
            @PathVariable Long productId,
            Model model
    ) {
        ProductSummary product = sellerProductService.find(productId);

        model.addAttribute(
                "product",
                product
        );
        model.addAttribute(
                "selectedCategoryId",
                product.categoryId()
        );
        model.addAttribute(
                "categoryOptions",
                sellerProductService.findCategoryOptions()
        );
        model.addAttribute(
                "stockForm",
                new ProductAddStockRequestDto(null)
        );

        return "seller/products/fragments/detail-modal :: detail";
    }

    @PostMapping
    public String register(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @ModelAttribute("registerForm") ProductRegisterRequestDto requestDto,
            BindingResult bindingResult,
            Model model

    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "result",
                    sellerProductService.findAll(
                            userId,
                            PageRequest.of(
                                    0,
                                    10
                            )
                    )
            );
            model.addAttribute(
                    "categoryOptions",
                    sellerProductService.findCategoryOptions()
            );
            model.addAttribute(
                    "openRegisterModal",
                    true
            );

            return "seller/products/list";
        }

        sellerProductService.register(
                userId,
                requestDto
        );
        return "redirect:/seller/products?notice=registered";
    }

    @PostMapping("/{productId}/stock")
    @PreAuthorize("@sellerProductOwnerValidator.isOwner(#productId, principal.userId)")
    public String addStock(
            @PathVariable("productId") Long productId,
            @Valid @ModelAttribute("stockForm") ProductAddStockRequestDto requestDto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/seller/products?notice=invalid-stock";
        }

        sellerProductService.addStock(
                productId,
                requestDto
        );
        return "redirect:/seller/products?notice=stock-added";
    }

    private boolean isOutOfRange(SellerProductPageResponseDto result) {
        return result.totalPages() > 0 && result.page() >= result.totalPages();
    }

    private String createRedirectUrl(int page, int size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/seller/products")
                .queryParam(
                        "page",
                        page
                )
                .queryParam(
                        "size",
                        size
                );

        return builder.build()
                .encode()
                .toUriString();
    }
}
