package com.commerce.pagopa.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.*;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class DataWebConfig {
}
