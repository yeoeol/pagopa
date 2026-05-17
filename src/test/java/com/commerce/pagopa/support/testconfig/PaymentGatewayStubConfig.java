package com.commerce.pagopa.support.testconfig;

import com.commerce.pagopa.payment.application.port.PaymentCancelResult;
import com.commerce.pagopa.payment.application.port.PaymentConfirmResult;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 동시성 테스트 전용 PaymentGateway 스텁
 * - 외부 Toss API 호출을 차단하고 즉시 success 응답 반환
 * - cancel/confirm 호출 횟수를 카운트해 중복 호출 여부를 검증할 수 있게 한다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PaymentGatewayStubConfig {

    @Bean
    @Primary
    public CountingPaymentGatewayStub paymentGatewayStub() {
        return new CountingPaymentGatewayStub();
    }

    public static class CountingPaymentGatewayStub implements PaymentGateway {

        private final AtomicInteger cancelCallCount = new AtomicInteger();
        private final AtomicInteger confirmCallCount = new AtomicInteger();

        @Override
        public PaymentConfirmResult confirm(String orderId, BigDecimal amount, String paymentKey) {
            confirmCallCount.incrementAndGet();
            return new PaymentConfirmResult(true, "DONE", paymentKey, amount, OffsetDateTime.now());
        }

        @Override
        public PaymentCancelResult cancel(String paymentKey, BigDecimal cancelAmount, String cancelReason) {
            cancelCallCount.incrementAndGet();
            return new PaymentCancelResult(true, "CANCELED", cancelAmount, BigDecimal.ZERO, OffsetDateTime.now());
        }

        public int cancelCallCount() {
            return cancelCallCount.get();
        }

        public int confirmCallCount() {
            return confirmCallCount.get();
        }

        public void reset() {
            cancelCallCount.set(0);
            confirmCallCount.set(0);
        }
    }
}
