@ApplicationModule(
        allowedDependencies = {
                "catalog :: api",
                "catalog :: event",
                "ordering :: event"
        }
)
package com.commerce.pagopa.recommendation;

import org.springframework.modulith.ApplicationModule;
