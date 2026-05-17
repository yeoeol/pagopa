package com.commerce.pagopa.concurrency;

import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.ProductOutOfStockException;
import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
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
class StockConcurrencyTest {

    @DynamicPropertySource
    static void hikariProps(DynamicPropertyRegistry registry) {
        // 대용량 동시성 테스트 한정: HikariCP 기본 풀(10) 으론 N=10000 풀 고갈 발생
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "50");
    }

    @Autowired
    OrderService orderService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UserRepository userRepository;

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 1000, 10000})
    void stock_10_으로_N명이_동시_주문하면_정확히_10명만_성공(int N) throws Exception {
        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        User seller = userRepository.save(UserFixture.aSeller("contention-" + N));
        Product product = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller));

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done   = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger other   = new AtomicInteger();
        Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

        long startNanos = System.nanoTime();
        for (int i = 0; i < N; i++) {
            pool.submit(() -> {
                try {
                    barrier.await();
                    orderService.order(seller.getId(), new OrderCreateRequestDto(PaymentMethod.CARD, new DeliveryRequestDto("test", "01012345678", "01010", "집주소", "101동", "메모"), List.of(new OrderCreateRequestDto.OrderProductRequestDto(product.getId(), 1))));
                    success.incrementAndGet();
                } catch (ProductOutOfStockException e) {
                    soldOut.incrementAndGet();
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
        log.info("[contention] N={} finished={} elapsed={}ms success={} soldOut={} other={}, finalStock={}",
                N, finished, elapsedMs, success.get(), soldOut.get(), other.get(), finalStock);
        errorCounts.forEach((k, v) -> log.warn("  [other] {} x{}", k, v));

        assertThat(success.get()).isEqualTo(10);
        assertThat(soldOut.get()).isEqualTo(N-10);
        assertThat(other.get()).isEqualTo(0);
        assertThat(finalStock).isZero();
    }

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 1000, 10000})
    void 서로_다른_N개_상품에_각각_1명씩_주문하면_경합없이_모두_성공(int N) throws Exception {
        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        User seller = userRepository.save(UserFixture.aSeller("no-contention-" + N));

        // N개 상품 미리 생성, 각 stock=1 → 동시 주문 시 row 경합 0
        List<Product> products = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            products.add(productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, 1)));
        }

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done   = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger other   = new AtomicInteger();
        Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

        long startNanos = System.nanoTime();
        for (int i = 0; i < N; i++) {
            Long productId = products.get(i).getId();
            pool.submit(() -> {
                try {
                    barrier.await();
                    orderService.order(seller.getId(), new OrderCreateRequestDto(PaymentMethod.CARD, new DeliveryRequestDto("test", "01012345678", "01010", "집주소", "101동", "메모"), List.of(new OrderCreateRequestDto.OrderProductRequestDto(productId, 1))));
                    success.incrementAndGet();
                } catch (ProductOutOfStockException e) {
                    soldOut.incrementAndGet();
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

        log.info("[no-contention] N={} finished={} elapsed={}ms success={} soldOut={} other={}",
                N, finished, elapsedMs, success.get(), soldOut.get(), other.get());
        errorCounts.forEach((k, v) -> log.warn("  [other] {} x{}", k, v));

        assertThat(success.get()).isEqualTo(N);
        assertThat(soldOut.get()).isZero();
        assertThat(other.get()).isZero();
    }
}
