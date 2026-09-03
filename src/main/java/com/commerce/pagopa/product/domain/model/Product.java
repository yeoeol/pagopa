package com.commerce.pagopa.product.domain.model;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.seller.domain.model.Seller;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "product",
        indexes = {
                @Index(
                        name = "idx_product_name",
                        columnList = "name"
                )
        }
)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "product_id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @ToString.Include
    @Column(name = "description", length = 255, nullable = true)
    private String description;

    @ToString.Include
    @Column(name = "price", nullable = false)
    private Integer price;

    @ToString.Include
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_category")
    )
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "seller_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_seller")
    )
    private Seller seller;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    @OrderBy("displayOrder ASC")
    private final List<ProductImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Product(
            String name,
            String description,
            Integer price,
            Integer stockQuantity,
            ProductStatus status,
            Category category,
            Seller seller
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.category = category;
        this.seller = seller;
    }

    public static Product create(
            String name,
            String description,
            Integer price,
            Integer stockQuantity,
            Category category,
            Seller seller
    ) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .seller(seller)
                .build();
    }

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.assignProduct(this);
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
        if (this.status == ProductStatus.SOLD_OUT && this.stockQuantity > 0) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public void decreaseStock(int quantity) {
        validateEnoughStock(quantity);
        this.stockQuantity -= quantity;
        if (this.stockQuantity <= 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    private void validateEnoughStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_OUT_OF_STOCK,
                    "productId=%d, 현재 재고=%d, 요청 수량=%d"
                            .formatted(this.id, this.stockQuantity, quantity)
            );
        }
    }
}
