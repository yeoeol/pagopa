@ApplicationModule(
		allowedDependencies = {
				"identity :: api",
				"catalog :: api"
		},
		type = OPEN
)
package com.commerce.pagopa.review;

import org.springframework.modulith.ApplicationModule;

import static org.springframework.modulith.ApplicationModule.Type.OPEN;