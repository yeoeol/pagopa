package com.commerce.pagopa.localtest.application;

import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.user.application.UserService;
import com.commerce.pagopa.user.application.dto.request.UserCreateRequestDto;
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
	private final UserService userService;
	private final AuthService authService;

	@Transactional
	public TokenResponseDto issueToken(String userKey) {
		String providerId = "load-test-" + userKey;

		UserCreateRequestDto requestDto = getUserCreateRequestDto(providerId);

		User user = userRepository.findByProviderAndProviderId(Provider.LOCAL_TEST, providerId)
				.orElseGet(() -> userService.register(requestDto));

		return authService.issueAccessTokenAndRefreshToken(
				user.getId(),
				user.getEmail(),
				user.getUserRoles().stream()
						.map(UserRole::getRole)
						.filter(Role::isEnabled)
						.map(Role::getCode)
						.map(RoleCode::name)
						.collect(Collectors.toUnmodifiableSet())
		);
	}

	private static UserCreateRequestDto getUserCreateRequestDto(String providerId) {
		return new UserCreateRequestDto(
				Provider.LOCAL_TEST,
				providerId,
				"test_user_" + UUID.randomUUID()
						.toString()
						.substring(0, 8),
				providerId + "@pagopa.local.test",
				"default.png"
		);
	}
}
