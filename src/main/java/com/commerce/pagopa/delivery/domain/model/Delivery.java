package com.commerce.pagopa.delivery.domain.model;

import com.commerce.pagopa.delivery.domain.model.enums.DeliveryStatus;
import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.order.domain.model.Order;
import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "delivery")
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "delivery_id", nullable = false)
    private Long id;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DeliveryStatus status;

    @ToString.Include
    @Column(name = "tracking_no", length = 50, nullable = true)
    private String trackingNo;

    @ToString.Include
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
            String requestMemo,
            Order order
    ) {
        return Delivery.builder()
                .address(address)
                .status(DeliveryStatus.PREPARING)
                .requestMemo(requestMemo)
                .order(order)
                .build();
    }

    public void ship(String trackingNo) {
        this.status = DeliveryStatus.SHIPPED;
        this.trackingNo = trackingNo;
    }
}
