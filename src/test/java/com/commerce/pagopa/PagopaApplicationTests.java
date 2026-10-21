package com.commerce.pagopa;

import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;

import com.commerce.pagopa.support.testcontainers.MySqlIntegrationTest;

@SpringBootTest
@MySqlIntegrationTest
class PagopaApplicationTests {

    @Test
    void contextLoads() {
    }
}
