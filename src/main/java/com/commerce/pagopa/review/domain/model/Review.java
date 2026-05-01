package com.commerce.pagopa.review.domain.model;

import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    private int rating;     // 1 ~ 5

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_product_id", nullable = false)
    private OrderProduct orderProduct;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Review(int rating, String content, User user, OrderProduct orderProduct) {
        this.rating = rating;
        this.content = content;
        this.user = user;
        this.orderProduct = orderProduct;
    }

    public static Review create(int rating, String content, User user, OrderProduct orderProduct) {
        return Review.builder()
                .rating(rating)
                .content(content)
                .user(user)
                .orderProduct(orderProduct)
                .build();
    }

    public void addImage(ReviewImage image) {
        this.images.add(image);
        image.assignReview(this);
    }

    public void update(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
