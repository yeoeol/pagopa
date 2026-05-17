package com.commerce.pagopa.concurrency;

import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto.OrderProductRequestDto;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.support.fixture.CategoryFixture;
import com.commerce.pagopa.support.fixture.CategoryFixture.CategoryTree;
import com.commerce.pagopa.support.fixture.PaymentFixture;
import com.commerce.pagopa.support.fixture.ProductFixture;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.support.testconfig.PaymentGatewayStubConfig;
import com.commerce.pagopa.support.testconfig.PaymentGatewayStubConfig.CountingPaymentGatewayStub;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 시나리오 A: 같은 주문에 대한 중복 취소 요청이 동시에 들어왔을 때
 * - 환불 API(PaymentGateway.cancel)는 정확히 1회만 호출되어야 한다.
 * - 1건만 성공, 나머지는 BusinessException 으로 차단되어야 한다.
 * - 결제 상태는 CANCELLED, 주문 상태도 CANCELLED 로 수렴.
 * - 차감했던 재고는 정확히 1회만 복원되어야 한다.
 */
@Slf4j
@SpringBootTest
@Import({TestcontainersConfig.class, PaymentGatewayStubConfig.class})
class OrderCancelConcurrencyTest {

    @DynamicPropertySource
    static void hikariProps(DynamicPropertyRegistry registry) {
        // 동시성 테스트에서 기본 풀(10) 고갈 회피
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "50");
    }

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired UserRepository userRepository;
    @Autowired CountingPaymentGatewayStub paymentGatewayStub;
    @Autowired PlatformTransactionManager transactionManager;

    @BeforeEach
    void resetStub() {
        paymentGatewayStub.reset();
    }

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 200, 1000})
    void 같은_주문을_N명이_동시_취소하면_정확히_1건만_성공(int N) throws Exception {
        // given: 결제 완료(PAID)된 취소 가능한 주문 1건 준비
        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        User seller = userRepository.save(UserFixture.aSeller("cancel-idem-seller-" + N));
        User buyer = userRepository.save(UserFixture.aBuyer("cancel-idem-buyer-" + N));

        Product product = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, 10));

        OrderResponseDto created = orderService.order(buyer.getId(),
                new OrderCreateRequestDto(
                        PaymentMethod.CARD,
                        new DeliveryRequestDto(
                                "test",
                                "01012345678",
                                "01010",
                                "집주소",
                                "101동",
                                "메모"
                        ),
                        List.of(new OrderProductRequestDto(product.getId(), 1))
                ));
        Long orderId = created.orderId();

        // 주문 → READY 전이 + PAID Payment 저장 (취소 가능 상태 만들기)
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Order order = orderRepository.findByIdOrThrow(orderId);
            order.pay();
            paymentRepository.save(PaymentFixture.aPaidPayment(order));
        });

        // when: N개 스레드가 동시에 같은 주문을 취소
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();
        Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

        OrderCancelRequestDto request = new OrderCancelRequestDto("동시성 테스트");

        long startNanos = System.nanoTime();
        for (int i = 0; i < N; i++) {
            pool.submit(() -> {
                try {
                    barrier.await();
                    orderService.cancelOrder(orderId, request);
                    success.incrementAndGet();
                } catch (BusinessException e) {
                    businessFail.incrementAndGet();
                    errorCounts.merge(e.getErrorCode().name(), 1, Integer::sum);
                } catch (Exception e) {
                    other.incrementAndGet();
                    errorCounts.merge(e.getClass().getSimpleName(), 1, Integer::sum);
                } finally {
                    done.countDown();
                }
            });
        }
        boolean finished = done.await(120, TimeUnit.SECONDS);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        pool.shutdown();

        // then
        Order finalOrder = orderRepository.findByIdOrThrow(orderId);
        Payment finalPayment = paymentRepository.findByOrder(finalOrder).orElseThrow();
        int finalStock = productRepository.findByIdOrThrow(product.getId()).getStock();

        log.info("[cancel-idempotency] N={} finished={} elapsed={}ms success={} businessFail={} other={} cancelApiCalls={} finalStock={} orderStatus={} paymentStatus={}",
                N, finished, elapsedMs, success.get(), businessFail.get(), other.get(),
                paymentGatewayStub.cancelCallCount(), finalStock, finalOrder.getStatus(), finalPayment.getStatus());
        errorCounts.forEach((k, v) -> log.warn("  [error] {} x{}", k, v));

        assertThat(success.get()).isEqualTo(1);
        assertThat(businessFail.get()).isEqualTo(N - 1);
        assertThat(other.get()).isZero();
        assertThat(paymentGatewayStub.cancelCallCount()).isEqualTo(1);
        assertThat(finalPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(finalStock).isEqualTo(10);
    }
}
