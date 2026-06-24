package com.commerce.pagopa.scrap.infrastructure.external;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.scrap.application.port.ScrapTargetValidator;
import com.commerce.pagopa.scrap.domain.model.EntityType;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import static com.commerce.pagopa.global.response.ErrorCode.PRODUCT_NOT_FOUND;

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
            throw new BusinessException(PRODUCT_NOT_FOUND);
        }
    }
}
