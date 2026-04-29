package com.commerce.pagopa.domain.product.validator;

import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("productOwnerValidator")
@RequiredArgsConstructor
public class ProductOwnerValidator extends OwnerValidator<Product> {

    private final ProductRepository productRepository;

    @Override
    protected Optional<Product> findResource(Long productId) {
        return productRepository.findById(productId);
    }

    @Override
    protected Long extractOwnerId(Product product) {
        return product.getSeller() == null ? null : product.getSeller().getId();
    }
}
