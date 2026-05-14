package com.commerce.pagopa.order.infrastructure.scheduler;

import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.domain.model.Order;
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
public class UnpaidOrderCancelScheduler {

    private static final int CHUNK_SIZE = 500;
    private static final long PAUSE_BETWEEN_CHUNKS_MS = 100;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${app.order.unpaid-before-min:15}")
    private long beforeMin;

    @PostConstruct
    void validateBeforeMin() {
        if (beforeMin <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "app.order.unpaid-before-min must be greater than 0"
            );
        }
    }

    /**
     * 매 분 0초 - 만료 미결제 주문 자동 취소
     * 청크(LIMIT) 단위 반복 - 단위 시간당 DB 피크 부하 평탄화, 사용자 요청과 공존
     */
    @Scheduled(cron = "0 * * * * *")
    public void cancelUnpaidOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(beforeMin);
        long totalCanceled = 0;
        int chunkNo = 0;

        while (true) {
            List<Order> unpaidOrderChunk = orderRepository.findUnpaidCreatedBefore(timeoutTime, CHUNK_SIZE);
            if (unpaidOrderChunk.isEmpty()) break;

            chunkNo++;
            for (Order order : unpaidOrderChunk) {
                try {
                    orderService.cancelUnpaidOrder(order.getId());
                    totalCanceled++;
                } catch (Exception e) {
                    log.error("[UnpaidOrderCancelScheduler] 자동 취소 실패 - Order ID: {}", order.getId(), e);
                }
            }

            // 청크 미완 = 대상 소진 - 다음 cron까지 대기
            if (unpaidOrderChunk.size() < CHUNK_SIZE) break;

            // DB CPU 피크 방지
            try {
                Thread.sleep(PAUSE_BETWEEN_CHUNKS_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[UnpaidOrderCancelScheduler] 인터럽트 - 청크 처리 중단");
                break;
            }
        }

        if (totalCanceled > 0) {
            log.info("[UnpaidOrderCancelScheduler] {}분 경과 미결제 주문 {}건 자동 취소 (chunks={})",
                    beforeMin, totalCanceled, chunkNo);
        }
    }
}
