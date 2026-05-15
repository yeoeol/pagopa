package com.commerce.pagopa.concurrency;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.ProductOutOfStockException;
import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
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
@Testcontainers
class StockConcurrencyTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

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
        Category root = createRootCategory("root");
        Category child1 = createChildCategory(root);
        Category child2 = createChildCategory(child1);
        categoryRepository.save(root);

        User seller = userRepository.save(
                createUser("test" + N, "test" + N, "test", Provider.NAVER, "3baac1ad-2c18-3922-81e8-4d032629ce92" + N, Role.ROLE_SELLER)
        );

        Product product = productRepository.save(createProduct("test", "test", BigDecimal.ONE, BigDecimal.ONE, 10, child2, seller));

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
        printLog(N, finished, elapsedMs, success, soldOut, other, finalStock);
        errorCounts.forEach((k, v) -> System.out.println("  [other] " + k + " x" + v));

        assertThat(success.get()).isEqualTo(10);
        assertThat(soldOut.get()).isEqualTo(N-10);
        assertThat(other.get()).isEqualTo(0);
        assertThat(finalStock).isZero();
    }

    private void printLog(int N, boolean finished, long elapsedMs, AtomicInteger success, AtomicInteger soldOut, AtomicInteger other, int finalStock) {
        log.debug("N={}", N);
        log.debug("finished = {}",finished);
        log.info("elapsed = {}", elapsedMs + "ms");
        log.info("success = {}", success.get());
        log.info("soldOut = {}", soldOut.get());
        log.info("other = {}", other.get());
        log.info("finalStock = {}", finalStock);
    }

    Category createRootCategory(String name) {
        return Category.createRoot(name);
    }

    Category createChildCategory(Category parent) {
        return parent.createChild("child1");
    }

    User createUser(
            String email, String nickname, String profileImage,
            Provider provider, String providerId, Role role
    ) {
        return User.create(
                email,
                nickname,
                profileImage,
                provider,
                providerId,
                role
        );
    }

    Product createProduct(
            String name, String description, BigDecimal price,
            BigDecimal discountPrice, int stock,
            Category category, User seller
    ) {
        return Product.create(
                name,
                description,
                price,
                discountPrice,
                stock,
                category,
                seller
        );
    }
}
