package com.commerce.pagopa.domain.order.entity;

import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.OrderCannotCancelException;
import com.commerce.pagopa.global.exception.OrderCannotPayException;
import com.commerce.pagopa.global.response.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private String orderNumber;

    @Column(nullable = false)
    private String orderName;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNumber, OrderStatus status, PaymentMethod paymentMethod, User user) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.user = user;
    }

    public static Order init(String orderNumber, PaymentMethod paymentMethod, User user) {
        return Order.builder()
                .orderNumber(orderNumber)
                .status(OrderStatus.ORDERED)
                .paymentMethod(paymentMethod)
                .user(user)
                .build();
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.add(orderProduct);
        orderProduct.assignOrder(this);
        calcTotalAmount();
    }

    public void assignDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.assignOrder(this);
    }

    public void assignOrderName(String orderName) {
        this.orderName = orderName;
    }

    public void markAsCancelled() {
        if (this.status != OrderStatus.ORDERED) {
            throw new OrderCannotCancelException();
        }
        this.updateStatus(OrderStatus.CANCELLED);
    }

    public void markAsPaid() {
        if (this.status != OrderStatus.ORDERED) {
            throw new OrderCannotPayException();
        }
        this.updateStatus(OrderStatus.PAID);
    }

    public void markAsOrdered() {
        this.updateStatus(OrderStatus.ORDERED);
    }

    public void markAsDelivering() {
        if (this.status != OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_DELIVER);
        }
        this.updateStatus(OrderStatus.DELIVERING);
    }

    public void markAsCompleted() {
        if (this.status != OrderStatus.DELIVERING) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_COMPLETE);
        }
        this.updateStatus(OrderStatus.COMPLETED);
    }

    private void updateStatus(OrderStatus status) {
        this.status = status;
    }

    private void calcTotalAmount() {
        this.totalAmount = orderProducts.stream()
                .map(OrderProduct::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
