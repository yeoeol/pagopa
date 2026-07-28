package com.commerce.pagopa.seller.presentation.security;

import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;

import org.springframework.stereotype.Component;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Component("sellerProductOwnerValidator")
@RequiredArgsConstructor
public class SellerProductOwnerValidator extends OwnerValidator<Product, Long> {

    private final ProductRepository productRepository;

    @Override
    protected Optional<Product> findResource(Long productId) {
        return productRepository.findById(productId);
    }

    @Override
    protected Long extractOwnerId(Product product) {
        return Optional.ofNullable(product.getSeller().getUser())
                .map(User::getId)
                .orElse(null);
    }
}
