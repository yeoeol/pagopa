package com.commerce.pagopa.domain.order.validator;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("orderOwnerValidator")
@RequiredArgsConstructor
public class OrderOwnerValidator {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long orderId, Long userId) {
        if (orderId == null || userId == null) {
            return false;
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getUser() == null) {
            return false;
        }
        return order.getUser().getId().equals(userId);
    }
}
