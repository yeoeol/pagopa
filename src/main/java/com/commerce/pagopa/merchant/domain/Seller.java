package com.commerce.pagopa.merchant.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "seller", uniqueConstraints = {@UniqueConstraint(name = "uq_seller_user_id", columnNames = {"user_id"})})
public class Seller extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "seller_id", nullable = false)
    private Long id;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SellerStatus status;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20, nullable = false)
    private VerificationStatus verificationStatus;

    @ToString.Include
    @Column(name = "status_changed_at", nullable = false)
    private LocalDateTime statusChangedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder(access = AccessLevel.PRIVATE)
    private Seller(
            SellerStatus status, VerificationStatus verificationStatus, LocalDateTime statusChangedAt,
            Long userId
    ) {
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.statusChangedAt = statusChangedAt;
        this.userId = userId;
    }

    public static Seller create(Long userId, LocalDateTime requestedAt) {
        return Seller.builder()
                .status(SellerStatus.PENDING)
                .verificationStatus(VerificationStatus.UNVERIFIED)
                .statusChangedAt(requestedAt)
                .userId(userId)
                .build();
    }

    public void requestAgain(LocalDateTime requestedAt) {
        if (this.status == SellerStatus.PENDING) {
            return;
        }
        if (this.status != SellerStatus.REJECTED) {
            throw new BusinessException(ErrorCode.SELLER_REQUEST_NOT_ALLOWED);
        }
        this.status = SellerStatus.PENDING;
        this.statusChangedAt = requestedAt;
    }

    public void activate(LocalDateTime activatedAt) {
        if (this.status != SellerStatus.PENDING) {
            throw new BusinessException(ErrorCode.SELLER_REQUEST_NOT_ALLOWED);
        }
        this.status = SellerStatus.ACTIVE;
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.statusChangedAt = activatedAt;
    }

    public void reject(LocalDateTime rejectedAt) {
        if (this.status != SellerStatus.PENDING) {
            throw new BusinessException(ErrorCode.SELLER_REQUEST_NOT_ALLOWED);
        }
        this.status = SellerStatus.REJECTED;
        this.verificationStatus = VerificationStatus.UNVERIFIED;
        this.statusChangedAt = rejectedAt;
    }
}
