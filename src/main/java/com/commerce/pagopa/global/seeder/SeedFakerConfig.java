package com.commerce.pagopa.global.seeder;

import net.datafaker.Faker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Locale;
import java.util.Random;

@Profile("local")
@Configuration
@EnableConfigurationProperties(SeedProperties.class)
class SeedFakerConfig {

    @Bean
    Faker faker() {
        return new Faker(Locale.KOREAN, new Random(42));
    }
}
