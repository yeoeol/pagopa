package com.commerce.pagopa.support.testcontainers;

import org.springframework.context.annotation.Bean;

import tools.jackson.databind.json.JsonMapper;

public class JacksonTestConfig {

    @Bean
    JsonMapper jsonMapper() {
        return new JsonMapper();
    }
}
