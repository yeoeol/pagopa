package com.commerce.pagopa.review.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_review_order_item_id",
                        columnNames = {"order_item_id"}
                )
        }
)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "review_id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "content", length = 100, nullable = false)
    private String content;

    @Min(1)
    @Max(5)
    @ToString.Include
    @Column(name = "rating", nullable = false)
    private Integer rating;     // 1 ~ 5

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_item_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_order_item")
    )
    private OrderItem orderItem;

    @OneToMany(
            mappedBy = "review",
            cascade = {
                    CascadeType.ALL,
                    CascadeType.REMOVE
            },
            orphanRemoval = true
    )
    private final List<ReviewImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Review(String content, Integer rating, OrderItem orderItem) {
        this.rating = rating;
        this.content = content;
        this.orderItem = orderItem;
    }

    public static Review create(String content, Integer rating, OrderItem orderItem) {
        return Review.builder()
                .content(content)
                .rating(rating)
                .orderItem(orderItem)
                .build();
    }

    public void addImage(ReviewImage image) {
        this.images.add(image);
        image.assignReview(this);
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
