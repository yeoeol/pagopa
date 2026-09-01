package com.commerce.pagopa.userrole.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.user.domain.model.User;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "user_role",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uq_user_role_user_id_role_id",
						columnNames = {"user_id", "role_id"}
				)
		}
)
public class UserRole extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_role_id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "user_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_user_role_user")
	)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "role_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_user_role_role")
	)
	private Role role;

	@Builder(access = AccessLevel.PRIVATE)
	private UserRole(
			User user,
			Role role
	) {
		this.user = user;
		this.role = role;
	}

	public static UserRole create(
			User user,
			Role role
	) {
		return UserRole.builder()
				.user(user)
				.role(role)
				.build();
	}

	public void assignUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "UserRole{" +
				"createdAt=" + createdAt +
				", id=" + id +
				", role=" + role +
				", updatedAt=" + updatedAt +
				'}';
	}
}
