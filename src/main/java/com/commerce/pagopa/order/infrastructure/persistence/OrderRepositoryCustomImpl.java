package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.QOrder;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.commerce.pagopa.order.domain.model.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findAllByPeriod(Long userId, OrderStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        List<Order> orders = queryFactory
                .selectFrom(order).distinct()
                .where(userIdEq(userId),
                        periodGoeAndLt(start, end),
                        statusEq(status))
                .orderBy(orderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(userIdEq(userId),
                        periodGoeAndLt(start, end),
                        statusEq(status))
                .fetchOne();

        return new PageImpl<>(orders, pageable, total == null ? 0L : total);
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId == null ? null : order.user.id.eq(userId);
    }

    private BooleanExpression periodGoeAndLt(LocalDateTime start, LocalDateTime end) {
        return (start == null || end == null) ? null : order.createdAt.goe(start).and(order.createdAt.lt(end));
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status == null ? null : order.status.eq(status);
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = pageable.getSort().stream()
                .map(this::orderSpecifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        if (orders.isEmpty()) {
            return new OrderSpecifier<?>[]{QOrder.order.id.desc()};
        }

        return orders.toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> orderSpecifier(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> order.isAscending() ? QOrder.order.id.asc() : QOrder.order.id.desc();
            case "createdAt" -> order.isAscending() ? QOrder.order.createdAt.asc() : QOrder.order.createdAt.desc();
            case "updatedAt" -> order.isAscending() ? QOrder.order.updatedAt.asc() : QOrder.order.updatedAt.desc();
            default -> null;
        };
    }
}
