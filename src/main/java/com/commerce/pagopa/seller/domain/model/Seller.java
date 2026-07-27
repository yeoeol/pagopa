package com.commerce.pagopa.seller.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
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
	@Column(name = "status", nullable = false)
	private SellerStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status", length = 20, nullable = false)
	private VerificationStatus verificationStatus;

	@Column(name = "activated_at", nullable = true)
	private Instant activatedAt;

	@Column(name = "suspended_at", nullable = true)
	private Instant suspendedAt;

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
			Instant activatedAt,
			Instant suspendedAt,
			User user
	) {
		this.status = status;
		this.verificationStatus = verificationStatus;
		this.activatedAt = activatedAt;
		this.suspendedAt = suspendedAt;
		this.user = user;
	}

	public static Seller create(User user) {
		return Seller.builder()
				.status(SellerStatus.PENDING)
				.verificationStatus(VerificationStatus.UNVERIFIED)
				.user(user)
				.build();
	}
}
