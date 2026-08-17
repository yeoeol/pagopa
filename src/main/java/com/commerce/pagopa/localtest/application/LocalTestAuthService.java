package com.commerce.pagopa.localtest.application;

import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.userrole.domain.model.UserRole;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalTestAuthService {

	private final UserRepository userRepository;
	private final AuthService authService;

	@Transactional
	public TokenResponseDto issueToken(String userKey) {
		String providerId = "load-test-" + userKey;


		User user = userRepository.findByProviderAndProviderId(Provider.LOCAL_TEST, providerId)
				.orElseGet(() -> userRepository.save(createUser(providerId)));

		return authService.issueAccessTokenAndRefreshToken(
				user.getId(),
				user.getEmail(),
				user.getUserRoles().stream()
						.map(UserRole::getRole)
						.map(Role::getCode)
						.map(RoleCode::name)
						.collect(Collectors.toUnmodifiableSet())
		);
	}

	private User createUser(String providerId) {
		return User.create(
				Provider.LOCAL_TEST,
				providerId,
				"user_" + UUID.randomUUID().toString().substring(0, 8),
				providerId + "@pagopa.local",
				"default.png"
		);
	}
}
