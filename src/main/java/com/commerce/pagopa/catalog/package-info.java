@ApplicationModule(
        allowedDependencies = {
                "identity :: api",
                "discovery :: event"
        }
)
package com.commerce.pagopa.catalog;

import org.springframework.modulith.ApplicationModule;
