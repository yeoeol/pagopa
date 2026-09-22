@ApplicationModule(
		allowedDependencies = {
				"catalog :: api",
				"basket :: api",
				"identity :: api"
		}
)
package com.commerce.pagopa.ordering;

import org.springframework.modulith.ApplicationModule;