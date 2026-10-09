@ApplicationModule(
        allowedDependencies = {
                "catalog :: event",
                "identity :: api"
        }
)
package com.commerce.pagopa.discovery;

import org.springframework.modulith.ApplicationModule;
