package com.commerce.pagopa.review.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.identity.api.ReviewAuthorQuery;
import com.commerce.pagopa.identity.api.ReviewAuthorSummary;
import com.commerce.pagopa.ordering.api.OrderItemApi;
import com.commerce.pagopa.ordering.api.OrderItemSummary;
import com.commerce.pagopa.review.application.dto.request.ReviewCreateRequestDto;
import com.commerce.pagopa.review.application.dto.request.ReviewUpdateRequestDto;
import com.commerce.pagopa.review.application.dto.response.ProductReviewResponseDto;
import com.commerce.pagopa.review.application.dto.response.ReviewResponseDto;
import com.commerce.pagopa.review.domain.Review;
import com.commerce.pagopa.review.domain.ReviewImage;
import com.commerce.pagopa.review.domain.ReviewRepository;

import static com.commerce.pagopa.global.response.ErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewAuthorQuery reviewAuthorQuery;
    private final ProductApi productApi;
    private final OrderItemApi orderItemApi;

    @Transactional
    public ReviewResponseDto create(Long userId, ReviewCreateRequestDto requestDto) {
        OrderItemSummary summary = orderItemApi.getReviewableOrderItem(userId, requestDto.orderItemId());

        Review review = Review.create(requestDto.content(), requestDto.rating(), summary.productId(),
                summary.orderItemId(), userId);

        for (int i = 0; i < requestDto.imageUrls()
                .size(); i++) {
            ReviewImage reviewImage = ReviewImage.create(requestDto.imageUrls()
                    .get(i), i + 1, review);
            review.addImage(reviewImage);
        }

        return ReviewResponseDto.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> findAll() {
        return reviewRepository.findAll()
                .stream()
                .map(ReviewResponseDto::from)
                .toList();
    }

    @Transactional
    public void update(Long reviewId, ReviewUpdateRequestDto requestDto) {
        Review review = reviewRepository.findByIdOrThrow(reviewId);
        review.update(requestDto.content(), requestDto.rating());
    }

    @Transactional
    public void delete(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponseDto> findAllByProduct(Long productId) {
        if (!productApi.existsById(productId)) {
            throw new BusinessException(PRODUCT_NOT_FOUND);
        }

        List<Review> reviews = reviewRepository.findAllWithDetailsByProductId(productId);

        Set<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .collect(Collectors.toSet());

        Map<Long, ReviewAuthorSummary> authors = reviewAuthorQuery.findAllByIds(userIds);

        return reviews.stream()
                .map(review -> ProductReviewResponseDto.from(review, authors.get(review.getUserId())))
                .toList();
    }
}
