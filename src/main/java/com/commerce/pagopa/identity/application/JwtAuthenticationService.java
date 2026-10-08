package com.commerce.pagopa.identity.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRepository;
import com.commerce.pagopa.identity.domain.UserStatus;
import com.commerce.pagopa.identity.infrastructure.jwt.AuthenticatedUser;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AuthenticatedUser loadActiveUser(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVE);
        }
        return AuthenticatedUser.from(user);
    }
}
