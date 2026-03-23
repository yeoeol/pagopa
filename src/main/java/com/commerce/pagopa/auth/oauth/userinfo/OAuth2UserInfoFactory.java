package com.commerce.pagopa.auth.oauth.userinfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo of(
            String registrationId,
            Map<String, Object> attributes
    ) {
        return switch (registrationId.toUpperCase()) {
            case "GOOGLE" -> new GoogleOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth2 Provider: " + registrationId);
        };
    }
}
