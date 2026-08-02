package com.commerce.pagopa.product.domain.model;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "product_image",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_product_image_product_id_display_order",
                        columnNames = {"product_id", "display_order"}
                )
        }
)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id", nullable = false)
    private Long id;

    @Column(name = "image_url", length = 512, nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_thumbnail", nullable = false)
    private boolean isThumbnail = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_image_product")
    )
    private Product product;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductImage(
            String imageUrl,
            int displayOrder,
            boolean isThumbnail,
            Product product
    ) {
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.isThumbnail = isThumbnail;
        this.product = product;
    }

    public static ProductImage create(
            String imageUrl,
            int displayOrder,
            boolean isThumbnail,
            Product product
    ) {
        return ProductImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .isThumbnail(isThumbnail)
                .product(product)
                .build();
    }

    public void assignProduct(Product product) {
        this.product = product;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
