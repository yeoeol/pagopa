package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.category.domain.model.QCategory;
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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.commerce.pagopa.category.domain.model.QCategory.category;
import static com.commerce.pagopa.product.domain.model.QProduct.product;
import static com.commerce.pagopa.product.domain.model.QProductImage.productImage;
import static com.commerce.pagopa.user.domain.model.QUser.user;
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
                .leftJoin(product.seller, user).fetchJoin()
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
        List<? extends OrderSpecifier<?>> orders = pageable.getSort().stream()
                .map(this::orderSpecifier)
                .filter(Objects::nonNull)
                .toList();

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
            case "discountPrice" -> order.isAscending() ? product.discountPrice.asc() : product.discountPrice.desc();
            case "stock" -> order.isAscending() ? product.stock.asc() : product.stock.desc();
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
