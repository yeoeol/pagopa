package com.commerce.pagopa.product.domain.model;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.user.domain.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User seller;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private final List<ProductImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Product(
            String name, String description, BigDecimal price,
            BigDecimal discountPrice, int stock,
            ProductStatus status, Category category, User seller
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.discountPrice = discountPrice;
        this.stock = stock;
        this.status = status;
        this.category = category;
        this.seller = seller;
    }

    public static Product create(
            String name, String description, BigDecimal price,
            BigDecimal discountPrice, int stock,
            Category category, User seller
    ) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .discountPrice(discountPrice)
                .stock(stock)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .seller(seller)
                .build();
    }

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.assignProduct(this);
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void markAsSoldOut() {
        this.status = ProductStatus.SOLDOUT;
    }

    public void hide() {
        this.status = ProductStatus.HIDDEN;
    }

    public void decreaseStock(int quantity) {
        validateOnSale();
        validatePositiveQuantity(quantity);
        validateEnoughStock(quantity);
        this.stock -= quantity;
    }

    public void restoreStock(int quantity) {
        validatePositiveQuantity(quantity);
        this.stock += quantity;
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }
    }

    private void validateOnSale() {
        if (this.status != ProductStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
    }

    private void validateEnoughStock(int quantity) {
        if (this.stock < quantity) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_OUT_OF_STOCK,
                    "재고가 부족합니다. productId=%d, 현재 재고=%d, 요청 수량=%d"
                            .formatted(this.id, this.stock, quantity)
            );
        }
    }
}
