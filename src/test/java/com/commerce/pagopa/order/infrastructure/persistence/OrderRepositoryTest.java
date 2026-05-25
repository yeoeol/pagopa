package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.support.fixture.OrderFixture;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@Import(TestcontainersConfig.class)
class OrderRepositoryTest {

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2025, 1, 1, 0, 0);

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @PersistenceContext
    EntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.aBuyer("order-period-test"));
    }

    @Test
    void findAllByPeriod_includesStartBoundaryAndExcludesEndBoundary() {
        // given: 경계 규칙 [start, end) 검증용 데이터
        Order atStart = persistOrder(user, START, OrderStatus.ORDERED);                                  // 포함 (>= start)
        Order inMiddle = persistOrder(user, LocalDateTime.of(2024, 6, 15, 10, 0), OrderStatus.ORDERED);  // 포함
        Order lastInstant = persistOrder(user, LocalDateTime.of(2024, 12, 31, 23, 59, 59), OrderStatus.ORDERED); // 포함
        persistOrder(user, LocalDateTime.of(2023, 12, 31, 23, 59, 59), OrderStatus.ORDERED);             // 제외 (< start)
        persistOrder(user, END, OrderStatus.ORDERED);                                                    // 제외 (== end)
        flushAndClear();

        // when
        Page<Order> result = orderRepository.findAllByPeriod(user.getId(), null, START, END, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Order::getId)
                .containsExactlyInAnyOrder(atStart.getId(), inMiddle.getId(), lastInstant.getId());
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findAllByPeriod_excludesOtherUsersOrders() {
        // given
        User other = userRepository.save(UserFixture.aBuyer("other-user"));
        Order mine = persistOrder(user, LocalDateTime.of(2024, 5, 1, 0, 0), OrderStatus.ORDERED);
        persistOrder(other, LocalDateTime.of(2024, 5, 1, 0, 0), OrderStatus.ORDERED);
        flushAndClear();

        // when
        Page<Order> result = orderRepository.findAllByPeriod(user.getId(), null, START, END, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Order::getId)
                .containsExactly(mine.getId());
    }

    @Test
    void findAllByPeriod_filtersByStatusWhenProvided() {
        // given
        Order ordered = persistOrder(user, LocalDateTime.of(2024, 3, 1, 0, 0), OrderStatus.ORDERED);
        Order cancelled = persistOrder(user, LocalDateTime.of(2024, 4, 1, 0, 0), OrderStatus.CANCELLED);
        flushAndClear();

        // when: status 지정 시 해당 상태만, null이면 전체
        Page<Order> onlyCancelled = orderRepository.findAllByPeriod(user.getId(), OrderStatus.CANCELLED, START, END, PageRequest.of(0, 10));
        Page<Order> all = orderRepository.findAllByPeriod(user.getId(), null, START, END, PageRequest.of(0, 10));

        // then
        assertThat(onlyCancelled.getContent())
                .extracting(Order::getId)
                .containsExactly(cancelled.getId());
        assertThat(all.getContent())
                .extracting(Order::getId)
                .containsExactlyInAnyOrder(ordered.getId(), cancelled.getId());
    }

    @Test
    void findAllByPeriod_totalCountReflectsFilterNotAllRows() {
        // given: 대상 3건 + 잡음 2건(타 유저 / 기간 밖) → count가 필터를 반영하는지(페이징 정확성)
        persistOrder(user, LocalDateTime.of(2024, 2, 1, 0, 0), OrderStatus.ORDERED);
        persistOrder(user, LocalDateTime.of(2024, 3, 1, 0, 0), OrderStatus.ORDERED);
        persistOrder(user, LocalDateTime.of(2024, 4, 1, 0, 0), OrderStatus.ORDERED);
        User other = userRepository.save(UserFixture.aBuyer("noise-user"));
        persistOrder(other, LocalDateTime.of(2024, 3, 1, 0, 0), OrderStatus.ORDERED); // 타 유저
        persistOrder(user, LocalDateTime.of(2023, 3, 1, 0, 0), OrderStatus.ORDERED);  // 기간 밖
        flushAndClear();

        // when: 페이지 크기 2
        Page<Order> firstPage = orderRepository.findAllByPeriod(user.getId(), null, START, END, PageRequest.of(0, 2));

        // then: 전체 5건이 아니라 필터된 3건 기준 count
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    private Order persistOrder(User buyer, LocalDateTime createdAt, OrderStatus status) {
        Order order = orderRepository.save(OrderFixture.anOrder(buyer));
        // createdAt은 @CreatedDate라 영속 시 now로 채워지므로 bulk update로 backdate, status도 함께 보정
        em.createQuery("update Order o set o.createdAt = :createdAt, o.status = :status where o.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("status", status)
                .setParameter("id", order.getId())
                .executeUpdate();
        return order;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
