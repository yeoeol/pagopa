package com.commerce.pagopa.order.application;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
class OrderTransactionService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public OrderCancelCommand prepareCancelOrder(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        BigDecimal cancelAmount = order.calculateActiveAmount();
        Payment payment = paymentRepository.getByOrderOrThrow(order);

        order.validateCancelable();
        payment.validateCancelable(cancelAmount);

        // 외부 PG 호출 직전 단일 트랜잭션만 통과시켜 중복 환불 방지
        int acquired = paymentRepository.acquireCancelLock(payment.getId());
        if (acquired == 0) {
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_IN_PROGRESS);
        }

        return new OrderCancelCommand(order.getId(), payment, cancelAmount);
    }

    @Transactional
    public SellerOrderCancelCommand prepareCancelSellerOrder(Long orderId, Long sellerOrderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        SellerOrder sellerOrder = order.findSellerOrder(sellerOrderId);
        BigDecimal cancelAmount = sellerOrder.getSellerTotalAmount();
        Payment payment = paymentRepository.getByOrderOrThrow(order);

        sellerOrder.validateBuyerCancelable();
        payment.validateCancelable(cancelAmount);

        return new SellerOrderCancelCommand(order.getId(), sellerOrder.getId(), payment, cancelAmount);
    }

    @Transactional
    public void markCancelOrderSuccess(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);

        order.cancel();
    }

    @Transactional
    public void markCancelSellerOrderSuccess(Long orderId, Long sellerOrderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        SellerOrder sellerOrder = order.findSellerOrder(sellerOrderId);

        sellerOrder.cancelByBuyer();
    }
}




