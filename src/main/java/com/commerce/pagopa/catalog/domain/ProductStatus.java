package com.commerce.pagopa.catalog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.DescribedStatus;

@Getter
@RequiredArgsConstructor
public enum ProductStatus implements DescribedStatus {
    ACTIVE("판매중"), INACTIVE("판매중지"), SOLD_OUT("품절"), HIDDEN("숨김"),;

    private final String description;
}
