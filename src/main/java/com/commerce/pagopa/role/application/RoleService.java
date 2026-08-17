package com.commerce.pagopa.role.application;

import com.commerce.pagopa.role.application.response.RoleResponseDto;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.repository.RoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

	private final RoleRepository roleRepository;

	@Transactional(readOnly = true)
	public List<RoleResponseDto> findAll(Boolean enabled) {
		List<Role> roles;
		if (enabled == null) {
			roles = roleRepository.findAll();
		} else {
			roles = roleRepository.findAllByEnabled(enabled);
		}

		return roles.stream()
				.map(RoleResponseDto::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public RoleResponseDto find(Long roleId) {
		return RoleResponseDto.from(roleRepository.findByIdOrThrow(roleId));
	}

	@Transactional
	public void inactive(Long roleId) {
		Role role = roleRepository.findByIdOrThrow(roleId);
		role.inactive();
	}
}
