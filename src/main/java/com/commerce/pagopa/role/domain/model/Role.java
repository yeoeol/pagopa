package com.commerce.pagopa.role.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "role",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uq_role_code",
						columnNames = {"code"}
				)
		}
)
public class Role extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "code", length = 20, nullable = false)
	private RoleCode code;

	@Column(name = "description", length = 50, nullable = false)
	private String description;

	@Column(name = "enabled", nullable = false)
	private boolean enabled = true;

	@Builder(access = AccessLevel.PRIVATE)
	private Role(
			RoleCode code,
			String description,
			boolean enabled
	) {
		this.code = code;
		this.description = description;
		this.enabled = enabled;
	}

	public static Role create(
			RoleCode code,
			String description
	) {
		return Role.builder()
				.code(code)
				.description(description)
				.enabled(true)
				.build();
	}

	public void active() {
		if (this.enabled) {
			throw new BusinessException(ErrorCode.ROLE_ALREADY_ENABLED);
		}
		this.enabled = true;
	}

	public void inactive() {
		if (!this.enabled) {
			throw new BusinessException(ErrorCode.ROLE_ALREADY_DISABLED);
		}
		this.enabled = false;
	}
}
