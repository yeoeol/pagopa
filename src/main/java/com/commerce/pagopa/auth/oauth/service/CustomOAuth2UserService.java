package com.commerce.pagopa.auth.oauth.service;

import com.commerce.pagopa.auth.oauth.userinfo.OAuth2UserInfo;
import com.commerce.pagopa.auth.oauth.userinfo.OAuth2UserInfoFactory;
import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.entity.enums.Provider;
import com.commerce.pagopa.domain.user.entity.enums.Role;
import com.commerce.pagopa.domain.user.entity.enums.UserStatus;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.auth.oauth.CustomOAuth2User;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.azure.base-url}")
    private String azureBaseUrl;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());
        User user = saveOrUpdate(userInfo, Provider.valueOf(registrationId.toUpperCase()));

        return new CustomOAuth2User(
                user,
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }

    private User saveOrUpdate(OAuth2UserInfo userInfo, Provider provider) {
        Optional<User> optionalUser = userRepository
                .findByProviderAndProviderId(provider, userInfo.getProviderId());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!user.getUserStatus().equals(UserStatus.ACTIVE)) {
                OAuth2Error error = new OAuth2Error(
                        ErrorCode.USER_NOT_ACTIVE.name(),
                        ErrorCode.USER_NOT_ACTIVE.getMessage(),
                        null
                );
                throw new OAuth2AuthenticationException(error, error.toString());
            }
            return user;
        }

        return userRepository.save(
                User.create(
                        userInfo.getEmail(),
                        "user_" + UUID.randomUUID().toString().substring(0, 8),
                        azureBaseUrl + "/default.png",
                        provider,
                        userInfo.getProviderId(),
                        Role.ROLE_USER
                )
        );
    }
}
