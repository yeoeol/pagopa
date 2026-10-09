package com.commerce.pagopa.identity.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.api.ReviewAuthorQuery;
import com.commerce.pagopa.identity.api.ReviewAuthorSummary;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewAuthorQueryService implements ReviewAuthorQuery {

    private final UserRepository userRepository;

    @Override
    public ReviewAuthorSummary findById(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        return toSummary(user);
    }

    @Override
    public Map<Long, ReviewAuthorSummary> findAllByIds(Collection<Long> userIds) {
        Map<Long, ReviewAuthorSummary> reviewAuthorSummary = new HashMap<>();

        List<User> users = userRepository.findByIdIn(userIds);
        users.forEach(
                user -> reviewAuthorSummary.put(
                        user.getId(),
                        toSummary(user)
                )
        );

        return reviewAuthorSummary;
    }

    private ReviewAuthorSummary toSummary(User user) {
        return new ReviewAuthorSummary(
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
