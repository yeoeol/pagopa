package com.commerce.pagopa.domain.payment.validator;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("paymentOwnerValidator")
@RequiredArgsConstructor
public class PaymentOwnerValidator {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(String orderNumber, Long userId) {
        if (orderNumber == null || userId == null) {
            return false;
        }

        Order order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order == null || order.getUser() == null) {
            return false;
        }

        return order.getUser().getId().equals(userId);
    }
}
