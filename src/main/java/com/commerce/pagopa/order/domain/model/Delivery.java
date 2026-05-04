package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "deliveries")
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @Embedded
    private Address address; // 값 타입(주소, 상세주소, 우편번호)

    @Column(nullable = false, length = 100)
    private String recipientName; // 수령인 이름

    @Column(nullable = false, length = 20)
    private String recipientPhone; // 수령인 연락처

    private String deliveryRequestMemo;

    @Builder(access = AccessLevel.PRIVATE)
    private Delivery(Address address, String recipientName, String recipientPhone, String deliveryRequestMemo) {
        this.address = address;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryRequestMemo = deliveryRequestMemo;
    }

    public static Delivery create(Address address, String recipientName, String recipientPhone, String deliveryRequestMemo) {
        return Delivery.builder()
                .address(address)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .deliveryRequestMemo(deliveryRequestMemo)
                .build();
    }
}
