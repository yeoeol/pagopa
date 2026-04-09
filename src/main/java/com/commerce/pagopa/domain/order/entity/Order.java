package com.commerce.pagopa.domain.order.entity;

import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(unique = true)
    private String orderNumber; // 20240101-XXXXX (비즈니스 식별자)

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNumber, BigDecimal totalAmount, OrderStatus status, PaymentMethod paymentMethod, User user, OrderItem... orderItems) {
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.user = user;
        this.orderItems.addAll(asList(orderItems));
    }

    public static Order create(String orderNumber, BigDecimal totalAmount, OrderStatus status, PaymentMethod paymentMethod, User user, OrderItem... orderItems) {
        return Order.builder()
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .status(status)
                .paymentMethod(paymentMethod)
                .user(user)
                .orderItems(orderItems)
                .build();
    }
}
