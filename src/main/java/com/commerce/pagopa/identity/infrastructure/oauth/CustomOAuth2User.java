package com.commerce.pagopa.identity.infrastructure.oauth;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.jspecify.annotations.Nullable;

import lombok.Getter;

import com.commerce.pagopa.identity.domain.Role;
import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRole;

@Getter
public class CustomOAuth2User implements OAuth2User, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;
    private final String attributeKey; // Provider별 고유 식별자 키

    public CustomOAuth2User(User user, Map<String, Object> attributes, String attributeKey) {
        this.user = user;
        this.attributes = attributes;
        this.attributeKey = attributeKey;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getUserRoles()
                .stream()
                .map(UserRole::getRole)
                .filter(Role::isEnabled)
                .map(Role::getCode)
                .map(RoleCode::name)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
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

    public Set<String> getRoles() {
        return user.getUserRoles()
                .stream()
                .map(UserRole::getRole)
                .filter(Role::isEnabled)
                .map(Role::getCode)
                .map(RoleCode::name)
                .collect(Collectors.toUnmodifiableSet());
    }
}
