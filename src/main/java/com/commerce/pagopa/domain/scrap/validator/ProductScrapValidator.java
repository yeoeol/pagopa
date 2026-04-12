package com.commerce.pagopa.domain.scrap.validator;

import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.scrap.entity.enums.EntityType;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductScrapValidator implements ScrapTargetValidator {

    private final ProductRepository productRepository;

    @Override
    public boolean supports(EntityType type) {
        return type == EntityType.PRODUCT;
    }

    @Override
    public void validate(Long targetId) {
        if (!productRepository.existsById(targetId)) {
            throw new ProductNotFoundException();
        }
    }
}