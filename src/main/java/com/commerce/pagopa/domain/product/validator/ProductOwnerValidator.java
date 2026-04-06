package com.commerce.pagopa.domain.product.validator;

import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("productOwnerValidator")
@RequiredArgsConstructor
public class ProductOwnerValidator {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long productId, Long userId) {
        if (productId == null || userId == null) {
            return false;
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getSeller() == null) {
            return false;
        }
        return product.getSeller().getId().equals(userId);
    }
}
