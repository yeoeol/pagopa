package com.commerce.pagopa;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.Test;

import com.commerce.pagopa.support.testcontainers.MySqlTestcontainersConfig;

@SpringBootTest
@Import(MySqlTestcontainersConfig.class)
class PagopaApplicationTests {

    @Test
    void contextLoads() {
    }
}
