package com.commerce.pagopa.domain.admin.review.service;

import com.commerce.pagopa.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public void delete(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
