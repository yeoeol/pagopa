package com.commerce.pagopa.review.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.REVIEW_NOT_FOUND;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long id);

    List<Review> findAll();

    void deleteById(Long id);

    Page<Review> findAllWithDetailsByProductId(
            Long productId,
            Pageable pageable
    );

    default Review findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(REVIEW_NOT_FOUND));
    }
}
