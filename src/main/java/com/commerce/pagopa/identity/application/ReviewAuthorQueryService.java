package com.commerce.pagopa.identity.application;

import com.commerce.pagopa.identity.api.ReviewAuthorQuery;
import com.commerce.pagopa.identity.api.ReviewAuthorSummary;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ReviewAuthorQueryService implements ReviewAuthorQuery {

	private final UserRepository userRepository;

	@Override
	public ReviewAuthorSummary findById(Long userId) {
		User user = userRepository.findByIdOrThrow(userId);
		return toSummary(user);
	}

	private ReviewAuthorSummary toSummary(User user) {
		return new ReviewAuthorSummary(
				user.getId(),
				user.getName(),
				user.getProfileImageUrl()
		);
	}
}
