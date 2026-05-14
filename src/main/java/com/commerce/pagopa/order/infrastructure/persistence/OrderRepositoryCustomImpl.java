package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

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
}
