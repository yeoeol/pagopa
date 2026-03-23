package com.commerce.pagopa.auth.oauth;

import com.commerce.pagopa.domain.user.entity.User;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;
    private final String attributeKey;  // Provider별 고유 식별자 키

    public CustomOAuth2User(User user, Map<String, Object> attributes, String attributeKey) {
        this.user = user;
        this.attributes = attributes;
        this.attributeKey = attributeKey;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getName() {
        return attributeKey;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getRole() {
        return user.getRole().name();
    }
}
