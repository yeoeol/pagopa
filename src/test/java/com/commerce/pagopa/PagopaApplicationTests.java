package com.commerce.pagopa;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.Test;

import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;

@SpringBootTest
@Import(TestcontainersConfig.class)
class PagopaApplicationTests {

    @Test
    void contextLoads() {
    }
}
