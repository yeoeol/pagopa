package com.commerce.pagopa.identity.localtest.application;

import com.commerce.pagopa.identity.application.AuthService;
import com.commerce.pagopa.identity.application.UserService;
import com.commerce.pagopa.identity.application.dto.request.UserCreateRequestDto;
import com.commerce.pagopa.identity.domain.*;
import com.commerce.pagopa.identity.infrastructure.jwt.TokenResponseDto;

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
