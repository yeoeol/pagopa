package com.commerce.pagopa.review.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.user.domain.model.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private OrderItem orderItem;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Review(int rating, String content, User user, OrderItem orderItem) {
        this.rating = rating;
        this.content = content;
        this.user = user;
        this.orderItem = orderItem;
    }

    public static Review create(int rating, String content, User user, OrderItem orderItem) {
        return Review.builder()
                .rating(rating)
                .content(content)
                .user(user)
                .orderItem(orderItem)
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
