package com.commerce.pagopa.identity.infrastructure.security.config;

import com.commerce.pagopa.global.cookie.GuestSessionCookieFactory;
import com.commerce.pagopa.identity.infrastructure.security.CurrentUserArgumentResolver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final GuestSessionCookieFactory guestSessionCookieFactory;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(
				new CurrentUserArgumentResolver(guestSessionCookieFactory)
		);
	}
}
