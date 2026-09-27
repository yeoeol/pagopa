package com.commerce.pagopa;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ApplicationModulithTest {

	private final ApplicationModules modules =
			ApplicationModules.of(PagopaApplication.class);

	@Test
	void verifiesModuleBoundaries() {
		modules.verify();
	}
}
