package com.commerce.pagopa.domain.order.repository;

import com.commerce.pagopa.domain.order.entity.Order;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.commerce.pagopa.domain.order.entity.QOrder.order;
import static com.commerce.pagopa.domain.order.entity.QOrderProduct.orderProduct;
import static com.commerce.pagopa.domain.product.entity.QProduct.product;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findAllBySellerId(Long sellerId, Pageable pageable) {
        // 1단계: 판매자 조건에 해당하는 order id만 페이징
        List<Long> orderIds = queryFactory
                .select(order.id)
                .from(order)
                .join(order.orderProducts, orderProduct)
                .join(orderProduct.product, product)
                .where(product.seller.id.eq(sellerId))
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (orderIds.isEmpty()) {
            return Page.empty(pageable);
        }

        long total = queryFactory
                .select(order.id.countDistinct())
                .from(order)
                .join(order.orderProducts, orderProduct)
                .join(orderProduct.product, product)
                .where(product.seller.id.eq(sellerId))
                .fetchOne();

        // 2단계: id 집합으로 orderProducts + product 전체를 fetch join
        List<Order> orders = queryFactory
                .selectFrom(order)
                .join(order.orderProducts, orderProduct).fetchJoin()
                .join(orderProduct.product, product).fetchJoin()
                .where(order.id.in(orderIds))
                .fetch();

        return new PageImpl<>(orders, pageable, total);
    }
}
