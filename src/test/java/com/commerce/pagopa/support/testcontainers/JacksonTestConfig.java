package com.commerce.pagopa.support.testcontainers;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.context.annotation.Bean;

public class JacksonTestConfig {

	@Bean
	JsonMapper jsonMapper() {
		return new JsonMapper();
	}
}
