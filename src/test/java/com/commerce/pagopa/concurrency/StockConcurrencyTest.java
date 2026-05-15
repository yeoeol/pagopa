package com.commerce.pagopa.concurrency;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.ProductOutOfStockException;
import com.commerce.pagopa.order.application.OrderService;
import com.commerce.pagopa.order.application.dto.request.DeliveryRequestDto;
import com.commerce.pagopa.order.application.dto.request.OrderCreateRequestDto;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void stock_10_으로_50명이_동시_주문하면_정확히_10명만_성공() throws Exception {
        Category root = createRootCategory("root");
        Category child1 = createChildCategory(root);
        Category child2 = createChildCategory(child1);
        categoryRepository.save(root);

        User seller = userRepository.save(
                createUser("test", "test", "test", Provider.NAVER, "3baac1ad-2c18-3922-81e8-4d032629ce92", Role.ROLE_SELLER)
        );

        Product product = productRepository.save(createProduct("test", "test", BigDecimal.ONE, BigDecimal.ONE, 10, child2, seller));

        int N = 50;

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        CyclicBarrier barrier = new CyclicBarrier(N);
        CountDownLatch done   = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger other   = new AtomicInteger();

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
                    System.out.println("[other] " + e.getClass().getSimpleName() + " : " + e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        boolean finished = done.await(15, TimeUnit.SECONDS);
        pool.shutdown();

        int finalStock = productRepository.findByIdOrThrow(product.getId()).getStock();
        System.out.println("finished=" + finished
                + " success=" + success.get()
                + " soldOut=" + soldOut.get()
                + " other=" + other.get()
                + " finalStock=" + finalStock);

        assertThat(success.get()).isEqualTo(10);
        assertThat(finalStock).isZero();
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
