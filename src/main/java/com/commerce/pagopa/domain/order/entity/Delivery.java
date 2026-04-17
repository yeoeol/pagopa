package com.commerce.pagopa.domain.order.entity;

import com.commerce.pagopa.domain.order.entity.enums.DeliveryStatus;
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

    @OneToOne(mappedBy = "delivery")
    private Order order;

    @Embedded
    private Address address; // 값 타입(주소, 상세주소, 우편번호)

    @Column(nullable = false, length = 100)
    private String recipientName; // 수령인 이름

    @Column(nullable = false, length = 20)
    private String recipientPhone; // 수령인 연락처

    private String deliveryRequestMemo; // 배송 요청사항 (예: 문 앞에 놓아주세요)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private Delivery(Address address, String recipientName, String recipientPhone, String deliveryRequestMemo, DeliveryStatus status) {
        this.address = address;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryRequestMemo = deliveryRequestMemo;
        this.status = status;
    }

    public static Delivery create(Address address, String recipientName, String recipientPhone, String deliveryRequestMemo) {
        return Delivery.builder()
                .address(address)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .deliveryRequestMemo(deliveryRequestMemo)
                .status(DeliveryStatus.READY) // 초기 배송 상태
                .build();
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

    public void updateStatus(DeliveryStatus status) {
        this.status = status;
    }
}
