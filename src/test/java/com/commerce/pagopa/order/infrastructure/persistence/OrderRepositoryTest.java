package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.repository.RoleRepository;
import com.commerce.pagopa.support.fixture.OrderFixture;
import com.commerce.pagopa.support.fixture.RoleFixture;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.support.fixture.UserRoleFixture;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@Import(TestcontainersConfig.class)
class OrderRepositoryTest {

    private static final Instant START = Instant.parse("2024-01-01T00:00:00.00Z");
    private static final Instant END = Instant.parse("2025-01-01T00:00:00.00Z");

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
	RoleRepository roleRepository;
    @PersistenceContext
    EntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.save(RoleFixture.aRoleUser());
        user = UserFixture.aUser("order-period-test");
        user.addUserRole(UserRoleFixture.aUserRole(user, userRole));
        user = userRepository.save(UserFixture.aUser("order-period-test"));
    }

    @Test
    void findAllByPeriod_includesStartBoundaryAndExcludesEndBoundary() {
        // given: 경계 규칙 [start, end) 검증용 데이터
        Order atStart = persistOrder(user, START, OrderStatus.CONFIRMED);                                  // 포함 (>= start)
        Order inMiddle = persistOrder(user, Instant.parse("2024-06-15T15:10:00.00Z"), OrderStatus.CONFIRMED);  // 포함
        Order lastInstant = persistOrder(user, Instant.parse("2024-12-31T23:59:59.00Z"), OrderStatus.CONFIRMED); // 포함
        persistOrder(user, Instant.parse("2023-12-31T23:59:59.00Z"), OrderStatus.CONFIRMED);             // 제외 (< start)
        persistOrder(user, END, OrderStatus.CONFIRMED);                                                    // 제외 (== end)
        flushAndClear();

        // when
        Page<Order> result = orderRepository.findAllByPeriod(
                user.getId(),
                null,
                START,
                END,
                PageRequest.of(0, 10)
        );

        // then
        System.out.println("result.size() = " + result.getSize());
        assertThat(result.getContent())
                .extracting(Order::getId)
                .containsExactlyInAnyOrder(
                        atStart.getId(),
                        inMiddle.getId(),
                        lastInstant.getId()
                );
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findAllByPeriod_excludesOtherUsersOrders() {
        // given
        User other = userRepository.save(UserFixture.aUser("other-user"));
        Order mine = persistOrder(user, Instant.parse("2024-05-01T00:00:00.00Z"), OrderStatus.CONFIRMED);
        persistOrder(other, Instant.parse("2024-05-01T00:00:00.00Z"), OrderStatus.CONFIRMED);
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
        Order ordered = persistOrder(user, Instant.parse("2024-03-01T00:00:00.00Z"), OrderStatus.CONFIRMED);
        Order cancelled = persistOrder(user, Instant.parse("2024-04-01T00:00:00.00Z"), OrderStatus.CANCELED);
        flushAndClear();

        // when: status 지정 시 해당 상태만, null이면 전체
        Page<Order> onlyCancelled = orderRepository.findAllByPeriod(user.getId(), OrderStatus.CANCELED, START, END, PageRequest.of(0, 10));
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
        persistOrder(user, Instant.parse("2024-02-01T00:00:00.00Z"), OrderStatus.CONFIRMED);
        persistOrder(user, Instant.parse("2024-03-01T00:00:00.00Z"), OrderStatus.CONFIRMED);
        persistOrder(user, Instant.parse("2024-04-01T00:00:00.00Z"), OrderStatus.CONFIRMED);
        User other = userRepository.save(UserFixture.aUser("noise-user"));
        persistOrder(other, Instant.parse("2024-03-01T00:00:00.00Z"), OrderStatus.CONFIRMED); // 타 유저
        persistOrder(user, Instant.parse("2023-03-01T00:00:00.00Z"), OrderStatus.CONFIRMED);  // 기간 밖
        flushAndClear();

        // when: 페이지 크기 2
        Page<Order> firstPage = orderRepository.findAllByPeriod(user.getId(), null, START, END, PageRequest.of(0, 2));

        // then: 전체 5건이 아니라 필터된 3건 기준 count
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    private Order persistOrder(User buyer, Instant orderedAt, OrderStatus status) {
        Order order = orderRepository.save(OrderFixture.anOrder(buyer));
        // createdAt은 @CreatedDate라 영속 시 now로 채워지므로 bulk update로 backdate, status도 함께 보정
        em.createQuery("""
                       update Order o
                       set o.orderedAt = :orderedAt, o.status = :status
                       where o.id = :id
                       """)
                .setParameter("orderedAt", orderedAt)
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
