package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
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
@Table(name = "seller_orders")
public class SellerOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_order_id")
    private Long id;

    /**
     * {orderNumber}-{seq} ex: ORD20260502-001-1
     */
    @Column(unique = true, nullable = false, length = 100)
    private String sellerOrderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerOrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellerTotalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToMany(mappedBy = "sellerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderProduct> orderProducts = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private SellerOrder(String sellerOrderNumber, SellerOrderStatus status, User seller) {
        this.sellerOrderNumber = sellerOrderNumber;
        this.status = status;
        this.sellerTotalAmount = BigDecimal.ZERO;
        this.seller = seller;
    }

    public static SellerOrder create(User seller, String sellerOrderNumber) {
        return SellerOrder.builder()
                .sellerOrderNumber(sellerOrderNumber)
                .status(SellerOrderStatus.PENDING_PAYMENT)
                .seller(seller)
                .build();
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.add(orderProduct);
        orderProduct.assignSellerOrder(this);
        recalcTotal();
    }

    public void pay() {
        if (this.status != SellerOrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.SELLER_ORDER_CANNOT_PAY);
        }
        this.status = SellerOrderStatus.READY;
    }

    public void deliver() {
        if (this.status != SellerOrderStatus.READY) {
            throw new BusinessException(ErrorCode.SELLER_ORDER_CANNOT_DELIVER);
        }
        this.status = SellerOrderStatus.DELIVERING;
    }

    public void complete() {
        if (this.status != SellerOrderStatus.DELIVERING) {
            throw new BusinessException(ErrorCode.SELLER_ORDER_CANNOT_COMPLETE);
        }
        this.status = SellerOrderStatus.COMPLETED;
    }

    public void validateCancelable() {
        if (this.status != SellerOrderStatus.PENDING_PAYMENT && this.status != SellerOrderStatus.READY) {
            throw new BusinessException(ErrorCode.SELLER_ORDER_CANNOT_CANCEL);
        }
    }

    /**
     * 결제 전(PENDING_PAYMENT) 또는 발송 전(READY) 상태에서만 취소 가능.
     * 취소 시 차감했던 상품 재고를 함께 복원한다 — 상태 전이와 재고 복원은 1회성으로 atomic하게 묶임.
     */
    public void cancel() {
        validateCancelable();
        this.status = SellerOrderStatus.CANCELLED;
        orderProducts.forEach(OrderProduct::restoreStock);
    }

    public boolean isCancelled() {
        return this.status == SellerOrderStatus.CANCELLED;
    }

    public boolean isCompleted() {
        return this.status == SellerOrderStatus.COMPLETED;
    }

    private void recalcTotal() {
        this.sellerTotalAmount = orderProducts.stream()
                .map(OrderProduct::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
