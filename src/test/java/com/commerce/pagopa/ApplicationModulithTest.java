package com.commerce.pagopa;

import org.springframework.modulith.core.ApplicationModules;

import org.junit.jupiter.api.Test;

public class ApplicationModulithTest {

    private final ApplicationModules modules = ApplicationModules.of(PagopaApplication.class);

    @Test
    void verifiesModuleBoundaries() {
        modules.verify();
    }
}
