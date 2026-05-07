package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.OrderCannotPayException;
import com.commerce.pagopa.global.exception.SellerOrderNotFoundException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /**
     * sync는 SellerOrder의 상태 전이 메서드(pay/deliver/complete/cancel)와
     * Order.addSellerOrder/refresh가 자동으로 트리거
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 한 Order의 모든 SellerOrder는 동일 배송지로 발송된다.
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<SellerOrder> sellerOrders = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNumber, PaymentMethod paymentMethod, User user, Delivery delivery) {
        this.orderNumber = orderNumber;
        this.paymentMethod = paymentMethod;
        this.user = user;
        this.delivery = delivery;
        this.totalAmount = BigDecimal.ZERO;
        this.orderName = "";
        this.status = OrderStatus.ORDERED;
    }

    public static Order init(String orderNumber, PaymentMethod paymentMethod, User user, Delivery delivery) {
        return Order.builder()
                .orderNumber(orderNumber)
                .paymentMethod(paymentMethod)
                .user(user)
                .delivery(delivery)
                .build();
    }

    /**
     * 한 Order에는 판매자당 SellerOrder가 정확히 1개 존재한다는 도메인 규칙을 강제
     * 해당 판매자의 SellerOrder가 이미 있으면 반환, 없으면 sellerOrderNumber 생성하여 추가 후 반환
     */
    public SellerOrder findOrCreateSellerOrderFor(User seller) {
        for (SellerOrder so : sellerOrders) {
            if (Objects.equals(so.getSeller(), seller)) {
                return so;
            }
        }
        SellerOrder newSellerOrder = SellerOrder.create(seller, "%s-%d".formatted(orderNumber, sellerOrders.size() + 1));
        addSellerOrder(newSellerOrder);
        return newSellerOrder;
    }

    public void addSellerOrder(SellerOrder sellerOrder) {
        for (SellerOrder so : sellerOrders) {
            if (Objects.equals(so.getSeller(), sellerOrder.getSeller())) {
                return;
            }
        }
        this.sellerOrders.add(sellerOrder);
        sellerOrder.assignOrder(this);
        recalcTotal();
        recomputeStatus();
    }

    /**
     * 항목 추가가 끝난 뒤 Service가 호출 — totalAmount/orderName/status를 일괄 재계산
     */
    public void refresh() {
        recalcTotal();
        this.orderName = deriveOrderName();
        recomputeStatus();
    }

    /**
     * SellerOrder의 상태 전이 메서드(pay/deliver/complete/cancel)가 호출하는 sync 트리거
     * 같은 패키지에서만 호출 — 외부에서는 도메인 메서드를 통해 간접 트리거
     */
    void recomputeStatus() {
        this.status = deriveStatus();
    }

    private void recalcTotal() {
        this.totalAmount = sellerOrders.stream()
                .map(SellerOrder::getSellerTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 첫 상품명을 기준으로 "{name} 외 N건" 형식의 표시용 이름을 도출 */
    private String deriveOrderName() {
        List<OrderProduct> all = sellerOrders.stream()
                .flatMap(so -> so.getOrderProducts().stream())
                .toList();
        if (all.isEmpty()) {
            return "";
        }
        String firstName = all.getFirst().getProduct().getName();
        return all.size() > 1
                ? "%s 외 %d건".formatted(firstName, all.size() - 1)
                : firstName;
    }

    /**
     * - 비어있음: ORDERED (초기 생성, SellerOrder 미부착)
     * - 모두 PENDING_PAYMENT → ORDERED
     * - 모두 CANCELLED → CANCELLED
     * - 모두 COMPLETED → COMPLETED
     * - 모두 READY → PAID
     * - 그 외 (혼재) → DELIVERING (= 진행 중)
     */
    private OrderStatus deriveStatus() {
        if (sellerOrders.isEmpty()) {
            return OrderStatus.ORDERED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.PENDING_PAYMENT)) {
            return OrderStatus.ORDERED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.CANCELLED)) {
            return OrderStatus.CANCELLED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.COMPLETED)) {
            return OrderStatus.COMPLETED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.READY)) {
            return OrderStatus.PAID;
        }
        return OrderStatus.DELIVERING;
    }

    private boolean allSellerOrdersAre(SellerOrderStatus status) {
        return sellerOrders.stream().allMatch(so -> so.getStatus() == status);
    }

    public void validatePayable() {
        if (status != OrderStatus.ORDERED) {
            throw new OrderCannotPayException();
        }
    }

    /**
     * 이 Order에 속한 SellerOrder를 id로 조회, 없으면 예외
     */
    public SellerOrder findSellerOrder(Long sellerOrderId) {
        return sellerOrders.stream()
                .filter(so -> so.getId().equals(sellerOrderId))
                .findFirst()
                .orElseThrow(SellerOrderNotFoundException::new);
    }

    /**
     * 취소되지 않은 SellerOrder들의 금액 합 — 환불 대상 잔여 금액 계산용
     */
    public BigDecimal calculateActiveAmount() {
        return sellerOrders.stream()
                .filter(so -> !so.isCancelled())
                .map(SellerOrder::getSellerTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 이 Order의 모든 SellerOrder가 CANCELLED 상태인지 — Payment 전체 취소 여부 결정용
     */
    public boolean isAllSellerOrdersCancelled() {
        return !sellerOrders.isEmpty() && sellerOrders.stream().allMatch(SellerOrder::isCancelled);
    }

    /**
     * 결제 승인 시: 모든 SellerOrder를 READY로 전환
     */
    public void pay() {
        validatePayable();
        sellerOrders.forEach(SellerOrder::pay);
    }

    /**
     * 전체 주문 취소: 취소 가능한 SellerOrder만 취소(이미 CANCELLED는 스킵)
     */
    public void cancel() {
        List<SellerOrder> activeSellerOrders = sellerOrders.stream()
                .filter(so -> !so.isCancelled())
                .toList();

        activeSellerOrders.forEach(SellerOrder::validateCancelable);
        activeSellerOrders.forEach(SellerOrder::cancel);
    }
}
