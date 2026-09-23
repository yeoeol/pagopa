package com.commerce.pagopa.merchant.presentation.security;

import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;
import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Component("sellerProductOwnerValidator")
@RequiredArgsConstructor
public class SellerProductOwnerValidator extends OwnerValidator<Product, Long> {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;

    @Override
    protected Optional<Product> findResource(Long productId) {
        return productRepository.findById(productId);
    }

    @Override
    protected Long extractOwnerId(Product product) {
        Seller seller = sellerRepository.findByIdOrThrow(product.getSellerId());
        return Optional.ofNullable(seller.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
