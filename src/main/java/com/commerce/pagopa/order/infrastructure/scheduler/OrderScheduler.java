package com.commerce.pagopa.order.infrastructure.scheduler;

import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${app.order.unpaid-before-min:15}")
    private long beforeMin;

    @PostConstruct
    void validateBeforeMin() {
        if (beforeMin <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "app.order.unpaid-before-min must be greater than 0");
        }
    }

    /**
     * 1분마다 실행되며, 주문 생성 후 {beforeMin}분이 지나도록 결제되지 않은(ORDERED) 주문을 찾아 자동 취소합니다.
     */
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void cancelUnpaidOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(beforeMin);

        // 상태가 ORDERED 이면서 생성 시간이 { beforeMin }분 이하
        List<Order> unpaidOrders = orderRepository.findByStatusAndCreatedAtLessThanEqual(OrderStatus.ORDERED, timeoutTime);

        if (!unpaidOrders.isEmpty()) {
            log.info("[OrderScheduler] {}분 경과 미결제 주문 {}건 자동 취소 시작", beforeMin, unpaidOrders.size());

            for (Order order : unpaidOrders) {
                try {
                    orderService.cancelUnpaidOrder(order.getId());
                    log.info("[OrderScheduler] 자동 취소 완료 - Order ID: {}", order.getId());
                } catch (Exception e) {
                    log.error("[OrderScheduler] 자동 취소 실패 - Order ID: {}", order.getId(), e);
                }
            }
        }
    }
}
