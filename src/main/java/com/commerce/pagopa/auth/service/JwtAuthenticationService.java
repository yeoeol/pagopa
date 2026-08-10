package com.commerce.pagopa.auth.service;

import com.commerce.pagopa.auth.jwt.AuthenticatedUser;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public AuthenticatedUser loadActiveUser(Long userId) {
		User user = userRepository.findByIdOrThrow(userId);
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.USER_NOT_ACTIVE);
		}
		return AuthenticatedUser.from(user);
	}
}
