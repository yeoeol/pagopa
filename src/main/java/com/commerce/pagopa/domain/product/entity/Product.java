package com.commerce.pagopa.domain.product.entity;

import com.commerce.pagopa.domain.category.entity.Category;
import com.commerce.pagopa.domain.product.entity.enums.ProductStatus;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.ProductOutOfStockException;
import com.commerce.pagopa.global.response.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    // 재고 증가 (비즈니스 로직 캡슐화)
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_QUANTITY);
        }
        this.stock += quantity;
    }

    // 재고 감소 (비즈니스 로직 캡슐화)
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_QUANTITY);
        }
        if (this.stock < quantity) {
            throw new ProductOutOfStockException();
        }
        this.stock -= quantity;
    }
}
