package com.commerce.pagopa.product.presentation.security;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("productOwnerValidator")
@RequiredArgsConstructor
public class ProductOwnerValidator extends OwnerValidator<Product, Long> {

    private final ProductRepository productRepository;

    @Override
    protected Optional<Product> findResource(Long productId) {
        return productRepository.findById(productId);
    }

    @Override
    protected Long extractOwnerId(Product product) {
        return Optional.ofNullable(product.getSeller())
                .map(User::getId)
                .orElse(null);
    }
}
