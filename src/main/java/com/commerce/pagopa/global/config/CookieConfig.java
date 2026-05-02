package com.commerce.pagopa.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CookieConfig {

    @Bean
    @Profile({"local", "dev"})
    public CookieSettings localDevCookieSettings() {
        return new CookieSettings(false);
    }

    @Bean
    @Profile("!local & !dev")
    public CookieSettings httpOnlyCookieSettings() {
        return new CookieSettings(true);
    }
}
