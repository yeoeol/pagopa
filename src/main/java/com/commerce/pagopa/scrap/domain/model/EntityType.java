package com.commerce.pagopa.scrap.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityType {
    PRODUCT("Product"),
    ;

    private final String description;
}
