package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static com.commerce.pagopa.order.domain.model.QOrder.order;
import static com.commerce.pagopa.order.domain.model.QSellerOrder.sellerOrder;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findUnpaidCreatedBefore(LocalDateTime timeoutTime, int limit) {
        return queryFactory
                .selectFrom(order).distinct()
                .join(order.sellerOrders, sellerOrder)
                .where(sellerOrder.status.eq(SellerOrderStatus.PENDING_PAYMENT)
                        .and(order.createdAt.loe(timeoutTime))
                )
                .orderBy(order.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Order> findAllByPeriod(Long userId, OrderStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        List<Order> orders = queryFactory
                .selectFrom(order).distinct()
                .where(userIdEq(userId),
                        periodGoeAndLt(start, end),
                        statusEq(status))
                .orderBy(order.createdAt.asc())
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
}
