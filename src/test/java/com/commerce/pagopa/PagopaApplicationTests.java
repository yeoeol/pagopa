package com.commerce.pagopa;

import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class PagopaApplicationTests {

    @Test
    void contextLoads() {
    }

}
