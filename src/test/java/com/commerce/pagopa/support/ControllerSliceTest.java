package com.commerce.pagopa.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

import com.commerce.pagopa.identity.infrastructure.jwt.JwtAuthenticationFilter;
import com.commerce.pagopa.identity.infrastructure.security.CurrentUserArgumentResolver;
import com.commerce.pagopa.identity.infrastructure.security.config.WebConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        JwtAuthenticationFilter.class,
                        CurrentUserArgumentResolver.class,
                        WebConfig.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
public @interface ControllerSliceTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] value() default {};
}
