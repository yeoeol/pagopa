package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

import static com.commerce.pagopa.order.domain.model.QOrder.order;
import static com.commerce.pagopa.order.domain.model.QSellerOrder.sellerOrder;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 셀러가 관여한 Order 목록을 페이징으로 조회
     */
    @Override
    public Page<Order> findAllBySellerId(Long sellerId, Pageable pageable) {
        List<Long> orderIds = queryFactory
                .select(order.id)
                .from(order)
                .join(order.sellerOrders, sellerOrder)
                .where(sellerOrder.seller.id.eq(sellerId))
                .distinct()
                .orderBy(order.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (orderIds.isEmpty()) {
            return Page.empty(pageable);
        }

        long total = queryFactory
                .select(order.id.countDistinct())
                .from(order)
                .join(order.sellerOrders, sellerOrder)
                .where(sellerOrder.seller.id.eq(sellerId))
                .fetchOne();

        List<Order> orders = queryFactory
                .selectFrom(order).distinct()
                .join(order.sellerOrders, sellerOrder).fetchJoin()
                .where(order.id.in(orderIds))
                .orderBy(order.createdAt.desc())
                .fetch();

        return new PageImpl<>(orders, pageable, total);
    }

    @Override
    public List<Order> findUnpaidCreatedBefore(@Param("timeoutTime") LocalDateTime timeoutTime) {
        return queryFactory
                .selectFrom(order).distinct()
                .join(order.sellerOrders, sellerOrder)
                .where(sellerOrder.status.eq(SellerOrderStatus.PENDING_PAYMENT)
                        .and(order.createdAt.loe(timeoutTime))
                ).fetch();
    }
}
