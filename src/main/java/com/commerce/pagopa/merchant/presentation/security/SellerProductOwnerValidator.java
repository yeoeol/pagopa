package com.commerce.pagopa.merchant.presentation.security;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Component("sellerProductOwnerValidator")
@RequiredArgsConstructor
public class SellerProductOwnerValidator extends OwnerValidator<ProductSummary, Long> {

    private final SellerRepository sellerRepository;
    private final ProductApi productApi;

    @Override
    protected Optional<ProductSummary> findResource(Long productId) {
        return Optional.ofNullable(productApi.get(productId));
    }

    @Override
    protected Long extractOwnerId(ProductSummary product) {
        Seller seller = sellerRepository.findByIdOrThrow(product.sellerId());
        return seller.getUserId();
    }
}
