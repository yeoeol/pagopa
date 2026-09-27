@ApplicationModule(
		allowedDependencies = {
				"discovery :: api",
				"identity :: api"
		}
)
package com.commerce.pagopa.catalog;

import org.springframework.modulith.ApplicationModule;
