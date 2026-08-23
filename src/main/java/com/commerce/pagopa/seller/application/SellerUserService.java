package com.commerce.pagopa.seller.application;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.repository.SellerRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.userrole.domain.model.UserRole;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerUserService {

	private final SellerRepository sellerRepository;
	private final UserRepository userRepository;

	@Transactional
	public void request(Long userId) {
		User user = userRepository.findByIdOrThrow(userId);
		validateRequestable(user);

		Seller seller = sellerRepository.findByUserId(userId)
				.map(existingSeller -> {
					existingSeller.requestAgain(Instant.now());
					return existingSeller;
				})
				.orElseGet(() -> Seller.create(user, Instant.now()));

		sellerRepository.save(seller);
	}

	private void validateRequestable(User user) {
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
