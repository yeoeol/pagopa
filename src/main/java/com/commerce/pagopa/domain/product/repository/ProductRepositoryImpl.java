package com.commerce.pagopa.domain.product.repository;

import com.commerce.pagopa.domain.product.dto.request.ProductSearchCondition;
import com.commerce.pagopa.domain.product.entity.Product;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.commerce.pagopa.domain.product.entity.QProduct.product;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> searchProducts(ProductSearchCondition condition) {
        return queryFactory
                .selectFrom(product)
                .where(nameContains(condition.productName()))
                .fetch();
    }

    private BooleanExpression nameContains(String name) {
        return hasText(name) ? product.name.contains(name) : null;
    }
}
