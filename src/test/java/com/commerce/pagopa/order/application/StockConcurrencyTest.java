package com.commerce.pagopa.order.application;

import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.delivery.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.orderitem.application.dto.request.OrderItemRequestDto;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.role.domain.repository.RoleRepository;
import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.repository.SellerRepository;
import com.commerce.pagopa.support.fixture.*;
import com.commerce.pagopa.support.fixture.CategoryFixture.CategoryTree;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

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
	RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
	SellerRepository sellerRepository;

    private User user;
    private Seller seller;
    private Role sellerRole;
    private Role userRole;

    @BeforeEach
    void beforeEach() {
        try {
            roleRepository.save(RoleFixture.aRoleAdmin());
            sellerRole = roleRepository.save(RoleFixture.aRoleSeller());
            userRole = roleRepository.save(RoleFixture.aRoleUser());
        } catch (Exception e) {
            sellerRole = roleRepository.findByCode(RoleCode.ROLE_SELLER).orElseThrow();
            userRole = roleRepository.findByCode(RoleCode.ROLE_USER).orElseThrow();
        }
    }

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 200, 1000})
    void stock_10_으로_N명이_동시_주문하면_정확히_10명만_성공(int N) throws Exception {
        user = UserFixture.aUser("order-stock-contention-seller-" + N);
        user.addUserRole(UserRoleFixture.aUserRole(user, userRole));
        user.addUserRole(UserRoleFixture.aUserRole(user, sellerRole));
        userRepository.save(user);
        seller = sellerRepository.save(SellerFixture.aSeller(user));

        User buyer = UserFixture.aUser("order-stock-contention-buyer-" + N);
        buyer.addUserRole(UserRoleFixture.aUserRole(buyer, userRole));
        userRepository.save(buyer);

        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        // 상품 등록
        Product product = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller));

        // 스레드풀 생성
        ExecutorService pool = Executors.newFixedThreadPool(N);

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
                    orderService.order(
                            buyer.getId(),
                            new OrderCreateRequestDto(
                                    new DeliveryRequestDto(
                                            "메모",
                                            "01010",
                                            "집주소",
                                            "101동"
                                    ),
                                    List.of(new OrderItemRequestDto(product.getId(), 1))
                            )
                    );
                    success.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode().equals(ErrorCode.PRODUCT_OUT_OF_STOCK)) {
                        soldOut.incrementAndGet();
                    }
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
        pool.close();

        int finalStock = productRepository.findByIdOrThrow(product.getId()).getStockQuantity();
        log.info("[contention] N={} finished={} elapsed={}ms success={} soldOut={} other={}, finalStock={}",
                N, finished, elapsedMs, success.get(), soldOut.get(), other.get(), finalStock);
        errorCounts.forEach((k, v) -> log.warn("  [other] {} x{}", k, v));

        assertThat(success.get()).isEqualTo(10);
        assertThat(soldOut.get()).isEqualTo(N-10);
        assertThat(other.get()).isEqualTo(0);
        assertThat(finalStock).isZero();
    }

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 200, 1000})
    void 서로_다른_N개_상품에_각각_1명씩_주문하면_경합없이_모두_성공(int N) throws Exception {
        user = UserFixture.aUser("order-stock-no-contention-seller-" + N);
        user.addUserRole(UserRoleFixture.aUserRole(user, userRole));
        user.addUserRole(UserRoleFixture.aUserRole(user, sellerRole));
        userRepository.save(user);
        seller = sellerRepository.save(SellerFixture.aSeller(user));

        User buyer = UserFixture.aUser("order-stock-no-contention-buyer-" + N);
        buyer.addUserRole(UserRoleFixture.aUserRole(buyer, userRole));
        userRepository.save(buyer);

        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        // N개 상품 미리 생성, 각 stockQuantity=1 → 동시 주문 시 row 경합 0
        List<Product> products = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            products.add(productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, 10+(i*5))));
        }

        ExecutorService pool = Executors.newFixedThreadPool(N);

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
                    orderService.order(
                            buyer.getId(),
                            new OrderCreateRequestDto(
                                    new DeliveryRequestDto(
                                            "메모",
                                            "01010",
                                            "집주소",
                                            "101동"
                                    ),
                                    List.of(new OrderItemRequestDto(productId, 1))
                            )
                    );
                    success.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode().equals(ErrorCode.PRODUCT_OUT_OF_STOCK)) {
                        soldOut.incrementAndGet();
                    }
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
        pool.close();

        log.info("[no-contention] N={} finished={} elapsed={}ms success={} soldOut={} other={}",
                N, finished, elapsedMs, success.get(), soldOut.get(), other.get());
        errorCounts.forEach((k, v) -> log.warn("  [other] {} x{}", k, v));

        assertThat(success.get()).isEqualTo(N);
        assertThat(soldOut.get()).isZero();
        assertThat(other.get()).isZero();
    }

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 200, 1000})
    void 동시_주문취소하면_정확히_1번만_성공(int N) throws Exception {
        user = UserFixture.aUser("cancel-idem-seller-" + N);
        user.addUserRole(UserRoleFixture.aUserRole(user, userRole));
        user.addUserRole(UserRoleFixture.aUserRole(user, sellerRole));
        userRepository.save(user);
        seller = sellerRepository.save(SellerFixture.aSeller(user));

        User buyer = UserFixture.aUser("cancel-idem-buyer-" + N);
        buyer.addUserRole(UserRoleFixture.aUserRole(buyer, userRole));
        userRepository.save(buyer);

        // 상품 등록
        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        Product product1 = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, 10));
        Product product2 = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, 20));
        Product product3 = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, 30));

        // 상품 주문
        OrderResponseDto created = orderService.order(buyer.getId(),
                new OrderCreateRequestDto(
                        new DeliveryRequestDto(
                                "메모",
                                "01010",
                                "집주소",
                                "101동"
                        ),
                        List.of(
                                new OrderItemRequestDto(product1.getId(), 1),
                                new OrderItemRequestDto(product2.getId(), 2),
                                new OrderItemRequestDto(product3.getId(), 3)
                        )
                )
        );
        Long orderId = created.orderId();

        // 스레드 풀 생성
        ExecutorService pool = Executors.newFixedThreadPool(N);

        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();
        Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

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
        pool.close();

        OrderResponseDto response = orderService.find(orderId);
        int finalStock1 = productRepository.findByIdOrThrow(product1.getId()).getStockQuantity();
        int finalStock2 = productRepository.findByIdOrThrow(product2.getId()).getStockQuantity();
        int finalStock3 = productRepository.findByIdOrThrow(product3.getId()).getStockQuantity();

        log.info("[same-order-cancel] N={} finished={} elapsed={}ms success={} businessFail={} other={} orderStatus={}",
                N, finished, elapsedMs, success.get(), businessFail.get(), other.get(), response.status());
        errorCounts.forEach((k, v) -> log.warn("  [error] {} x{}", k, v));

        assertThat(finished).isTrue();
        assertThat(success.get()).isEqualTo(1);
        assertThat(businessFail.get()).isEqualTo(N - 1);
        assertThat(other.get()).isZero();
        assertThat(response.status().status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(response.status().description()).isEqualTo("주문취소");
        assertThat(finalStock1).isEqualTo(10);
        assertThat(finalStock2).isEqualTo(20);
        assertThat(finalStock3).isEqualTo(30);
    }

    @ParameterizedTest(name = "N={0}")
    @ValueSource(ints = {50, 200, 1000})
    void 서로_다른_N개_상품에_각각_1명씩_주문취소하면_경합없이_재고_복구_성공(int N) throws Exception {
        user = UserFixture.aUser("cross-order-seller-" + N);
        user.addUserRole(UserRoleFixture.aUserRole(user, userRole));
        user.addUserRole(UserRoleFixture.aUserRole(user, sellerRole));
        userRepository.save(user);
        seller = sellerRepository.save(SellerFixture.aSeller(user));

        User buyer = UserFixture.aUser("cross-order-buyer-" + N);
        buyer.addUserRole(UserRoleFixture.aUserRole(buyer, userRole));
        userRepository.save(buyer);

        // 상품 등록
        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());

        Product product = productRepository.save(ProductFixture.aProduct(tree.leaf(), seller, N));

        // 상품 주문
        List<Long> orderIds = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            OrderResponseDto created = orderService.order(buyer.getId(),
                    new OrderCreateRequestDto(
                            new DeliveryRequestDto(
                                    "메모",
                                    "01010",
                                    "집주소",
                                    "101동"
                            ),
                            List.of(new OrderItemRequestDto(product.getId(), 1))
                    )
            );
            orderIds.add(created.orderId());
        }

        // 주문 성공 검증
        assertThat(productRepository.findByIdOrThrow(product.getId()).getStockQuantity()).isZero();

        // 스레드 풀 생성
        ExecutorService pool = Executors.newFixedThreadPool(N);

        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();
        Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

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
        pool.close();

        int finalStock = productRepository.findByIdOrThrow(product.getId()).getStockQuantity();

        log.info("[different-order-cancel] N={} finished={} elapsed={}ms success={} businessFail={} other={} finalStock={}",
                N, finished, elapsedMs, success.get(), businessFail.get(), other.get(), finalStock);
        errorCounts.forEach((k, v) -> log.warn("  [error] {} x{}", k, v));

        assertThat(finished).isTrue();
        assertThat(success.get()).isEqualTo(N);
        assertThat(businessFail.get()).isZero();
        assertThat(other.get()).isZero();
        assertThat(finalStock).isEqualTo(N);
    }
}
