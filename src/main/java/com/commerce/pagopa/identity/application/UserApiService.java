package com.commerce.pagopa.identity.application;

import com.commerce.pagopa.identity.api.UserApi;
import com.commerce.pagopa.identity.domain.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApiService implements UserApi {

	private final UserRepository userRepository;

	@Override
	public boolean existsById(Long userId) {
		return userRepository.existsById(userId);
	}
}
