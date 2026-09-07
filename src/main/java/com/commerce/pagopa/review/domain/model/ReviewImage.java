package com.commerce.pagopa.review.domain.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "review_image",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_review_image_review_id_display_order",
                        columnNames = {"review_id", "display_order"}
                )
        }
)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "review_image_id")
    private Long id;

    @ToString.Include
    @Column(name = "image_url", length = 512, nullable = false)
    private String imageUrl;

    @ToString.Include
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "review_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_image_review")
    )
    private Review review;

    @Builder(access = AccessLevel.PRIVATE)
    private ReviewImage(String imageUrl, int displayOrder, Review review) {
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.review = review;
    }

    public static ReviewImage create(String imageUrl, int displayOrder, Review review) {
        return ReviewImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .review(review)
                .build();
    }
}
