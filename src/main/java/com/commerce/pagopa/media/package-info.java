@ApplicationModule(
        allowedDependencies = {
                "identity :: event",
                "review :: event"
        }
)
package com.commerce.pagopa.media;

import org.springframework.modulith.ApplicationModule;
