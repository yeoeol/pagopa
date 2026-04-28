package com.commerce.pagopa.domain.order.scheduler;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 1분마다 실행되며, 주문 생성 후 15분이 지나도록 결제되지 않은(ORDERED) 주문을 찾아 자동 취소합니다.
     */
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void cancelUnpaidOrders() {
        // 15분 이전 시간 계산
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(15);
        
        // 상태가 ORDERED 이면서 생성 시간이 15분 이하(정확히 15분 전 포함)인 주문들 조회
        List<Order> unpaidOrders = orderRepository.findByStatusAndCreatedAtLessThanEqual(OrderStatus.ORDERED, timeoutTime);

        if (!unpaidOrders.isEmpty()) {
            log.info("[OrderScheduler] 15분 경과 미결제 주문 {}건 자동 취소 시작", unpaidOrders.size());
            
            for (Order order : unpaidOrders) {
                try {
                    // OrderService의 cancelOrder 재사용하여 재고 원복까지 안전하게 처리
                    orderService.cancelOrder(order.getId());
                    log.info("[OrderScheduler] 자동 취소 완료 - Order ID: {}", order.getId());
                } catch (Exception e) {
                    log.error("[OrderScheduler] 자동 취소 실패 - Order ID: {}", order.getId(), e);
                }
            }
        }
    }
}
