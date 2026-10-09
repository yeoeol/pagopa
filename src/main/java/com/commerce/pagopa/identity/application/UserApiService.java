package com.commerce.pagopa.identity.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.identity.api.UserApi;
import com.commerce.pagopa.identity.api.UserSummary;
import com.commerce.pagopa.identity.domain.*;

@Service
@RequiredArgsConstructor
public class UserApiService implements UserApi {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserSummary get(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        return toSummary(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserSummary> findAllByIdIn(Collection<Long> userIds) {
        List<User> users = userRepository.findByIdIn(userIds);
        return users.stream()
                .collect(
                        Collectors.toMap(
                                User::getId,
                                this::toSummary
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional
    public void grantSellerRole(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        Role sellerRole = roleRepository.findByCode(RoleCode.ROLE_SELLER)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        user.grantRole(sellerRole);
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
