package com.commerce.pagopa.support.testcontainers;

import java.lang.annotation.*;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ActiveProfiles("test")
@Import(
    {
            MySqlTestcontainersConfig.class,
            DatabaseCleanupConfig.class,
            MockVectorStoreTestConfig.class
    }
)
public @interface MySqlIntegrationTest {
}
