package com.commerce.pagopa.catalog.infrastructure.persistence;

import com.commerce.pagopa.catalog.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductStatus;
import com.commerce.pagopa.catalog.domain.QCategory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.commerce.pagopa.catalog.domain.QCategory.category;
import static com.commerce.pagopa.catalog.domain.QProduct.product;
import static com.commerce.pagopa.catalog.domain.QProductImage.productImage;
import static com.commerce.pagopa.merchant.domain.QSeller.seller;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        List<Product> products = queryFactory
                .selectFrom(product)
                .where(statusEq(ProductStatus.ACTIVE)
                        .or(statusEq(ProductStatus.SOLD_OUT)))
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(statusEq(ProductStatus.ACTIVE)
                        .or(statusEq(ProductStatus.SOLD_OUT)))
                .fetchOne();

        return new PageImpl<>(products, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<Product> findAllByCategoryOrAncestorCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    ) {
        QCategory parentCategory = new QCategory("parentCategory");
        QCategory grandParentCategory = new QCategory("grandParentCategory");

        List<Product> products = queryFactory
                .selectFrom(product)
                .leftJoin(product.category, category).fetchJoin()
                .leftJoin(category.parent, parentCategory)
                .leftJoin(parentCategory.parent, grandParentCategory)
                .where(
                        categoryOrAncestorCategoryIdEq(categoryId, parentCategory, grandParentCategory),
                        product.status.in(statuses)
                )
                .orderBy(orderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(product.category, category)
                .leftJoin(category.parent, parentCategory)
                .leftJoin(parentCategory.parent, grandParentCategory)
                .where(
                        categoryOrAncestorCategoryIdEq(categoryId, parentCategory, grandParentCategory),
                        product.status.in(statuses)
                )
                .fetchOne();

        return new PageImpl<>(products, pageable, total == null ? 0L : total);
    }

    @Override
    public List<Product> searchProducts(@NonNull ProductSearchCondition condition) {
        return queryFactory
                .selectFrom(product).distinct()
                .leftJoin(product.seller, seller).fetchJoin()
                .leftJoin(product.category, category).fetchJoin()
                .leftJoin(product.images, productImage).fetchJoin()
                .where(nameContains(condition.productName()))
                .fetch();
    }

    private BooleanExpression nameContains(String name) {
        return hasText(name) ? product.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression categoryOrAncestorCategoryIdEq(
            Long categoryId,
            QCategory parentCategory,
            QCategory grandParentCategory
    ) {
        return category.id.eq(categoryId)
                .or(parentCategory.id.eq(categoryId))
                .or(grandParentCategory.id.eq(categoryId));
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = pageable.getSort().stream()
                .map(this::orderSpecifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        if (orders.isEmpty()) {
            return new OrderSpecifier<?>[]{product.id.desc()};
        }

        return orders.toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> orderSpecifier(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> order.isAscending() ? product.id.asc() : product.id.desc();
            case "name" -> order.isAscending() ? product.name.asc() : product.name.desc();
            case "price" -> order.isAscending() ? product.price.asc() : product.price.desc();
            case "stockQuantity" -> order.isAscending() ? product.stockQuantity.asc() : product.stockQuantity.desc();
            case "status" -> order.isAscending() ? product.status.asc() : product.status.desc();
            case "createdAt" -> order.isAscending() ? product.createdAt.asc() : product.createdAt.desc();
            case "updatedAt" -> order.isAscending() ? product.updatedAt.asc() : product.updatedAt.desc();
            default -> null;
        };
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status == null ? null : product.status.eq(status);
    }
}
