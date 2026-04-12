package com.commerce.pagopa.domain.scrap.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityType {
    PRODUCT("Product"),
    ;

    private final String description;
}
