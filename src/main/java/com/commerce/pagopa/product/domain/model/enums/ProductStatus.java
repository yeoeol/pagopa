package com.commerce.pagopa.product.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ACTIVE("판매중"),
    INACTIVE("판매중지"),
    SOLDOUT("품절"),
    HIDDEN("숨김"),
    ;

    private final String description;
}
