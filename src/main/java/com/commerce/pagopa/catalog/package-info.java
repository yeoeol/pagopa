@ApplicationModule(
		allowedDependencies = {
				"discovery :: api"
		},
		type = OPEN
)
package com.commerce.pagopa.catalog;

import org.springframework.modulith.ApplicationModule;

import static org.springframework.modulith.ApplicationModule.Type.OPEN;