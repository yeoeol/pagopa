package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
class PaymentTransactionService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public boolean prepareConfirm(PaymentApproveRequestDto requestDto) {
        Order order = orderRepository.getByOrderNumberOrThrow(requestDto.orderId());
        Payment payment = paymentRepository.getByOrderOrThrow(order);

        payment.validateConfirmable();
        order.validatePayable();

        if (!payment.isAmountMatched(requestDto.amount())) {
            payment.fail();
            order.cancel();
            return false;
        }
        return true;
    }

    @Transactional
    public void markConfirmSuccess(String orderNumber, String paymentKey) {
        Order order = orderRepository.getByOrderNumberOrThrow(orderNumber);
        Payment payment = paymentRepository.getByOrderOrThrow(order);

        payment.success(paymentKey);
        order.pay();
    }

    @Transactional
    public void markConfirmFailure(String orderNumber) {
        Order order = orderRepository.getByOrderNumberOrThrow(orderNumber);
        Payment payment = paymentRepository.getByOrderOrThrow(order);

        payment.fail();
        order.cancel();
    }

    @Transactional
    public PaymentCancelCommand prepareCancel(Payment payment, BigDecimal cancelAmount) {
        payment.validateCancelable(cancelAmount);
        return new PaymentCancelCommand(payment.getId(), payment.getPaymentKey(), cancelAmount);
    }

    @Transactional
    public void markCancelSuccess(Long paymentId, BigDecimal cancelAmount) {
        Payment payment = paymentRepository.getByIdOrThrow(paymentId);

        payment.cancel(cancelAmount);
    }
}
