package com.commerce.pagopa.order.application;

import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.order.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCancelRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderProductRequestDto;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.support.fixture.CategoryFixture;
import com.commerce.pagopa.support.fixture.CategoryFixture.CategoryTree;
import com.commerce.pagopa.support.fixture.ProductFixture;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
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

@Slf4j
@SpringBootTest
@Import(TestcontainersConfig.class)
class OrderCancelConcurrencyTest {

    @DynamicPropertySource
    static void hikariProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "50");
    }

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired UserRepository userRepository;
    @Autowired PlatformTransactionManager transactionManager;

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 200, 1000})
    void sameOrderConcurrentCancel_succeedsOnceAndRestoresStockOnce(int N) throws Exception {
        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        User seller = userRepository.save(UserFixture.aSeller("cancel-idem-seller-" + N));
        User buyer = userRepository.save(UserFixture.aBuyer("cancel-idem-buyer-" + N));
        Product product = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, 10));

        OrderResponseDto created = orderService.order(buyer.getId(),
                new OrderCreateRequestDto(
                        new DeliveryRequestDto("test", "01012345678", "01010", "address", "101", "memo"),
                        List.of(new OrderProductRequestDto(product.getId(), 1))
                ));
        Long orderId = created.orderId();

        payOrder(orderId);

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();
        Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

        OrderCancelRequestDto request = new OrderCancelRequestDto("concurrent cancel");

        long startNanos = System.nanoTime();
        for (int i = 0; i < N; i++) {
            pool.submit(() -> {
                try {
                    barrier.await();
                    orderService.cancelOrder(orderId);
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

        Order finalOrder = orderRepository.findByIdOrThrow(orderId);
        int finalStock = productRepository.findByIdOrThrow(product.getId()).getStock();

        log.info("[same-order-cancel] N={} finished={} elapsed={}ms success={} businessFail={} other={} finalStock={} orderStatus={}",
                N, finished, elapsedMs, success.get(), businessFail.get(), other.get(), finalStock, finalOrder.getStatus());
        errorCounts.forEach((k, v) -> log.warn("  [error] {} x{}", k, v));

        assertThat(finished).isTrue();
        assertThat(success.get()).isEqualTo(1);
        assertThat(businessFail.get()).isEqualTo(N - 1);
        assertThat(other.get()).isZero();
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(finalStock).isEqualTo(10);
    }

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 200, 1000})
    void differentOrdersConcurrentCancel_restoresAllStock(int N) throws Exception {
        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        User seller = userRepository.save(UserFixture.aSeller("cross-order-seller-" + N));
        User buyer = userRepository.save(UserFixture.aBuyer("cross-order-buyer-" + N));
        Product product = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, N));

        List<Long> orderIds = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            OrderResponseDto created = orderService.order(buyer.getId(),
                    new OrderCreateRequestDto(
                            new DeliveryRequestDto(
                                    "test",
                                    "01012345678",
                                    "01010",
                                    "address",
                                    "101",
                                    "memo"
                            ),
                            List.of(new OrderProductRequestDto(product.getId(), 1))
                    )
            );
            orderIds.add(created.orderId());
            payOrder(created.orderId());
        }

        assertThat(productRepository.findByIdOrThrow(product.getId()).getStock()).isZero();

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();
        Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

        OrderCancelRequestDto request = new OrderCancelRequestDto("concurrent cancel");

        long startNanos = System.nanoTime();
        for (Long orderId : orderIds) {
            pool.submit(() -> {
                try {
                    barrier.await();
                    orderService.cancelOrder(orderId);
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

        int finalStock = productRepository.findByIdOrThrow(product.getId()).getStock();

        log.info("[different-order-cancel] N={} finished={} elapsed={}ms success={} businessFail={} other={} finalStock={}",
                N, finished, elapsedMs, success.get(), businessFail.get(), other.get(), finalStock);
        errorCounts.forEach((k, v) -> log.warn("  [error] {} x{}", k, v));

        assertThat(finished).isTrue();
        assertThat(success.get()).isEqualTo(N);
        assertThat(businessFail.get()).isZero();
        assertThat(other.get()).isZero();
        assertThat(finalStock).isEqualTo(N);
    }

    private void payOrder(Long orderId) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Order order = orderRepository.findByIdOrThrow(orderId);
        });
    }
}
