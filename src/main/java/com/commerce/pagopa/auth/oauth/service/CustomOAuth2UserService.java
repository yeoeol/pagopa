package com.commerce.pagopa.auth.oauth.service;

import com.commerce.pagopa.auth.oauth.CustomOAuth2User;
import com.commerce.pagopa.auth.oauth.userinfo.OAuth2UserInfo;
import com.commerce.pagopa.auth.oauth.userinfo.OAuth2UserInfoFactory;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.user.application.UserService;
import com.commerce.pagopa.user.application.dto.request.UserCreateRequestDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserService userService;

	private static final String ADMIN_SUFFIX = "-admin";

	@Value("${app.azure.base-url}")
    private String azureBaseUrl;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        boolean adminLogin = registrationId.endsWith(ADMIN_SUFFIX);

        String providerRegistrationId = resolveProviderRegistrationId(registrationId);
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(providerRegistrationId, oAuth2User.getAttributes());
        Provider provider = Provider.valueOf(providerRegistrationId.toUpperCase());
        User user = adminLogin
                ? findExistingActiveUser(userInfo, provider)
                : findOrRegisterActiveUser(userInfo, provider);

        return new CustomOAuth2User(
                user,
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }

    private String resolveProviderRegistrationId(String registrationId) {
        if (registrationId.endsWith(ADMIN_SUFFIX)) {
            return registrationId.substring(0, registrationId.length() - ADMIN_SUFFIX.length());
        }
        return registrationId;
    }

    private User findExistingActiveUser(OAuth2UserInfo userInfo, Provider provider) {
        return findActiveUser(userInfo, provider)
				.orElseThrow(() -> {
                    OAuth2Error error = new OAuth2Error(
                            ErrorCode.USER_NOT_FOUND.name(),
                            ErrorCode.USER_NOT_FOUND.getMessage(),
                            null
                    );
                    return new OAuth2AuthenticationException(error, error.toString());
                });
    }

    private User findOrRegisterActiveUser(OAuth2UserInfo userInfo, Provider provider) {
		return findActiveUser(userInfo, provider)
				.orElseGet(() -> userService.register(
						new UserCreateRequestDto(
								provider,
								userInfo.getProviderId(),
								userInfo.getName(),
								userInfo.getEmail(),
								azureBaseUrl + "/default.png"
						)
				));
    }

	private Optional<User> findActiveUser(OAuth2UserInfo userInfo, Provider provider) {
		return userService.findByProviderAndProviderId(provider, userInfo.getProviderId())
				.map(user -> {
					if (user.getStatus() != UserStatus.ACTIVE) {
						OAuth2Error error = new OAuth2Error(
								ErrorCode.USER_NOT_ACTIVE.name(),
								ErrorCode.USER_NOT_ACTIVE.getMessage(),
								null
						);
						throw new OAuth2AuthenticationException(error, error.toString());
					}
					return user;
				});
	}
}
