package com.commerce.pagopa.domain.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageCategory {
    PROFILE("profile"),
    PRODUCT("product"),
    REVIEW("review"),
    ;

    private final String directory;
}
