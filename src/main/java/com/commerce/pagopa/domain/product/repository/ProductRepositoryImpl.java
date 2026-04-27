package com.commerce.pagopa.domain.product.repository;

import com.commerce.pagopa.domain.product.dto.request.ProductSearchCondition;
import com.commerce.pagopa.domain.product.entity.Product;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.commerce.pagopa.domain.product.entity.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> searchProducts(ProductSearchCondition condition) {
        return queryFactory
                .selectFrom(product)
                .where(nameEq(condition.productName()))
                .fetch();
    }

    private BooleanExpression nameEq(String name) {
        return StringUtils.hasText(name) ? null : product.name.eq(name);
    }
}
