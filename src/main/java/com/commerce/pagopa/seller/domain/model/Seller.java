package com.commerce.pagopa.seller.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.seller.domain.model.enums.SellerStatus;
import com.commerce.pagopa.seller.domain.model.enums.VerificationStatus;
import com.commerce.pagopa.user.domain.model.User;

import jakarta.persistence.*;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "seller",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uq_seller_user_id",
						columnNames = {"user_id"}
				)
		}
)
public class Seller extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "seller_id", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private SellerStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status", length = 20, nullable = false)
	private VerificationStatus verificationStatus;

	@Column(name = "status_changed_at", nullable = false)
	private Instant statusChangedAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "user_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_seller_user")
	)
	private User user;

	@Builder(access = AccessLevel.PRIVATE)
	private Seller(
			SellerStatus status,
			VerificationStatus verificationStatus,
			Instant statusChangedAt,
			User user
	) {
		this.status = status;
		this.verificationStatus = verificationStatus;
		this.statusChangedAt = statusChangedAt;
		this.user = user;
	}

	public static Seller create(User user, Instant requestedAt) {
		return Seller.builder()
				.status(SellerStatus.PENDING)
				.verificationStatus(VerificationStatus.UNVERIFIED)
				.statusChangedAt(requestedAt)
				.user(user)
				.build();
	}

	public void requestAgain(Instant requestedAt) {
		if (this.status == SellerStatus.PENDING) {
			return;
		}
		if (this.status != SellerStatus.REJECTED) {
			throw new BusinessException(ErrorCode.SELLER_REQUEST_NOT_ALLOWED);
		}
		this.status = SellerStatus.PENDING;
		this.statusChangedAt = requestedAt;
	}

	public void activate(Instant activatedAt) {
		if (this.status != SellerStatus.PENDING) {
			throw new BusinessException(ErrorCode.SELLER_REQUEST_NOT_ALLOWED);
		}
		this.status = SellerStatus.ACTIVE;
		this.verificationStatus = VerificationStatus.VERIFIED;
		this.statusChangedAt = activatedAt;
	}

	public void reject(Instant rejectedAt) {
		if (this.status != SellerStatus.PENDING) {
			throw new BusinessException(ErrorCode.SELLER_REQUEST_NOT_ALLOWED);
		}
		this.status = SellerStatus.REJECTED;
		this.verificationStatus = VerificationStatus.UNVERIFIED;
		this.statusChangedAt = rejectedAt;
	}
}
