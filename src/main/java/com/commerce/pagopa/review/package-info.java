@ApplicationModule(
		allowedDependencies = {
				"identity :: api",
				"catalog :: api",
				"ordering :: api"
		},
		type = OPEN
)
package com.commerce.pagopa.review;

import org.springframework.modulith.ApplicationModule;

import static org.springframework.modulith.ApplicationModule.Type.OPEN;