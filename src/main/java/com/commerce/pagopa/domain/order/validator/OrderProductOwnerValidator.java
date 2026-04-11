package com.commerce.pagopa.domain.order.validator;

import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.domain.order.repository.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("orderProductOwnerValidator")
@RequiredArgsConstructor
public class OrderProductOwnerValidator {

    private final OrderProductRepository orderProductRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long orderProductId, Long userId) {
        if (orderProductId == null || userId == null) {
            return false;
        }

        OrderProduct orderProduct = orderProductRepository.findByIdWithOrderAndUser(orderProductId).orElse(null);
        if (orderProduct == null || orderProduct.getOrder() == null || orderProduct.getOrder().getUser() == null) {
            return false;
        }

        return orderProduct.getOrder().getUser().getId().equals(userId);
    }
}