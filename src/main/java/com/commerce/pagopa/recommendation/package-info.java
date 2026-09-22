@ApplicationModule(
		allowedDependencies = {
				"catalog :: api",
				"ordering :: event",
				"discovery :: event"
		}
)
package com.commerce.pagopa.recommendation;

import org.springframework.modulith.ApplicationModule;