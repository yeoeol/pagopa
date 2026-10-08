package com.commerce.pagopa.review.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.*;

import com.commerce.pagopa.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "review", uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_order_item_id", columnNames = {"order_item_id"})})
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "review_id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "content", length = 100, nullable = false)
    private String content;

    @Min(1) @Max(5) @ToString.Include
    @Column(name = "rating", nullable = false)
    private Integer rating; // 1 ~ 5

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "review", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private final List<ReviewImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Review(String content, Integer rating, Long productId, Long orderItemId, Long userId) {
        this.content = content;
        this.rating = rating;
        this.productId = productId;
        this.orderItemId = orderItemId;
        this.userId = userId;
    }

    public static Review create(String content, Integer rating, Long productId, Long orderItemId, Long userId) {
        return Review.builder()
                .content(content)
                .rating(rating)
                .productId(productId)
                .orderItemId(orderItemId)
                .userId(userId)
                .build();
    }

    public void addImage(ReviewImage image) {
        this.images.add(image);
    }

    public void update(String content, Integer rating) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if (rating != null && (1 <= rating && rating <= 5)) {
            this.rating = rating;
        }
    }
}
