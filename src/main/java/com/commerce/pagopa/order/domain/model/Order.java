package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_status", columnList = "user_id,status")
})
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private String orderName;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 한 Order의 모든 SellerOrder는 동일 배송지로 발송된다.
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderProduct> orderProducts = new ArrayList<>();


    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNumber, User user, Delivery delivery, LocalDateTime orderedAt, LocalDateTime cancelledAt) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.delivery = delivery;
        this.totalAmount = BigDecimal.ZERO;
        this.orderName = "";
        this.status = OrderStatus.ORDERED;
        this.orderedAt = orderedAt;
        this.cancelledAt = cancelledAt;
    }

    public static Order init(User user, Delivery delivery) {
        return Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .delivery(delivery)
                .orderedAt(LocalDateTime.now())
                .cancelledAt(null)
                .build();
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }

    public void addTotalPrice(BigDecimal totalPrice) {
        this.totalAmount = this.totalAmount.add(totalPrice);
    }

    public void setCancelledAt(LocalDateTime now) {
        this.cancelledAt = now;
    }

    private static String generateOrderNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
