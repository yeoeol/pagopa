package com.commerce.pagopa.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long id;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean isThumbnail = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductImage(
            String imageUrl, int displayOrder, boolean isThumbnail
    ) {
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.isThumbnail = isThumbnail;
    }

    public static ProductImage create(String imageUrl, int displayOrder, boolean isThumbnail) {
        return ProductImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .isThumbnail(isThumbnail)
                .build();
    }

    public void assignProduct(Product product) {
        this.product = product;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}