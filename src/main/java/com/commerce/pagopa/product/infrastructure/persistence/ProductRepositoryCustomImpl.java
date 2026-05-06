package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.commerce.pagopa.product.domain.model.QProduct.product;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        List<Product> products = queryFactory
                .selectFrom(product)
                .where(statusEq(ProductStatus.ACTIVE)
                        .or(statusEq(ProductStatus.SOLDOUT)))
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(statusEq(ProductStatus.ACTIVE)
                        .or(statusEq(ProductStatus.SOLDOUT)))
                .fetchOne();

        return new PageImpl<>(products, pageable, total == null ? 0L : total);
    }

    @Override
    public List<Product> searchProducts(@NonNull ProductSearchCondition condition) {
        return queryFactory
                .selectFrom(product)
                .where(nameContains(condition.productName()))
                .fetch();
    }

    private BooleanExpression nameContains(String name) {
        return hasText(name) ? product.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status == null ? null : product.status.eq(status);
    }
}
