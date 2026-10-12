package com.commerce.pagopa.media.api;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface ContentSource {
    InputStream openStream() throws IOException;
}
