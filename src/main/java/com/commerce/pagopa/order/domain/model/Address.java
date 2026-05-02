package com.commerce.pagopa.order.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    private String zipcode;         // 우편번호
    private String address;         // 주소
    private String detailAddress;   // 상세주소

}
