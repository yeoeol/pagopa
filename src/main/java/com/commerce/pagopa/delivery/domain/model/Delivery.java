package com.commerce.pagopa.delivery.domain.model;

import com.commerce.pagopa.delivery.domain.model.enums.DeliveryStatus;
import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.order.domain.model.Order;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "delivery")
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DeliveryStatus status;

    @Column(name = "tracking_no", length = 50, nullable = true)
    private String trackingNo;

    @Column(name = "request_memo", length = 100, nullable = true)
    private String requestMemo;

    @Embedded
    private Address address; // 값 타입(주소, 상세주소, 우편번호)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_delivery_orders")
    )
    private Order order;

    @Builder(access = AccessLevel.PRIVATE)
    private Delivery(
            DeliveryStatus status,
            String trackingNo,
            Address address,
            String requestMemo,
            Order order
    ) {
        this.status = status;
        this.trackingNo = trackingNo;
        this.address = address;
        this.requestMemo = requestMemo;
        this.order = order;
    }

    public static Delivery create(
            Address address,
            String trackingNo,
            String requestMemo,
            Order order
    ) {
        return Delivery.builder()
                .address(address)
                .status(DeliveryStatus.PREPARING)
                .trackingNo(trackingNo)
                .requestMemo(requestMemo)
                .order(order)
                .build();
    }
}
