package com.commerce.pagopa.review.application;

import com.commerce.pagopa.global.exception.ProductNotFoundException;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.repository.OrderProductRepository;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.review.application.dto.request.ReviewCreateRequestDto;
import com.commerce.pagopa.review.application.dto.request.ReviewUpdateRequestDto;
import com.commerce.pagopa.review.application.dto.response.ProductReviewResponseDto;
import com.commerce.pagopa.review.application.dto.response.ReviewResponseDto;
import com.commerce.pagopa.review.domain.model.Review;
import com.commerce.pagopa.review.domain.model.ReviewImage;
import com.commerce.pagopa.review.domain.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewResponseDto create(Long userId, ReviewCreateRequestDto requestDto) {
        User user = userRepository.findByIdOrThrow(userId);
        OrderProduct orderProduct = orderProductRepository.getByIdOrThrow(requestDto.orderProductId());

        Review review = Review.create(requestDto.rating(), requestDto.content(), user, orderProduct);

        for (int i = 0; i < requestDto.imageUrls().size(); i++) {
            ReviewImage reviewImage = ReviewImage.create(requestDto.imageUrls().get(i), i + 1);
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
        review.update(requestDto.rating(), requestDto.content());
    }

    @Transactional
    public void delete(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponseDto> findAllByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException();
        }

        return reviewRepository.findAllByProductIdWithUserAndReviewImages(productId).stream()
                .map(ProductReviewResponseDto::from)
                .toList();
    }
}
