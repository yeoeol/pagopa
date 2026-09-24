package com.commerce.pagopa.identity.application;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.identity.api.UserValidateApi;
import com.commerce.pagopa.identity.domain.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserValidateApiService implements UserValidateApi {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public void validateRequestable(Long userId) {
		User user = userRepository.findByIdForUpdateOrThrow(userId);

		boolean hasAlreadySellerRole = user.getUserRoles()
				.stream()
				.map(UserRole::getRole)
				.map(Role::getCode)
				.anyMatch(roleCode -> roleCode == RoleCode.ROLE_SELLER);

		if (hasAlreadySellerRole) {
			throw new BusinessException(ErrorCode.ROLE_ALREADY_EXISTS);
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.USER_NOT_ACTIVE);
		}
	}
}
