package com.commerce.pagopa.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "zipcode", length = 10)
    private String zipcode;         // 우편번호

    @Column(name = "address", length = 100)
    private String address;         // 주소

    @Column(name = "detail_address", length = 100)
    private String detailAddress;   // 상세주소

    @Builder(access = AccessLevel.PRIVATE)
    private Address(String zipcode, String address, String detailAddress) {
        this.zipcode = zipcode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public static Address create(String zipcode, String address, String detailAddress) {
        return Address.builder()
                .zipcode(zipcode)
                .address(address)
                .detailAddress(detailAddress)
                .build();
    }
}
