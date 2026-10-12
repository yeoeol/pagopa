package com.commerce.pagopa.identity.application;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.application.dto.request.UserCreateRequestDto;
import com.commerce.pagopa.identity.application.dto.request.UserUpdateRequestDto;
import com.commerce.pagopa.identity.application.dto.response.UserResponseDto;
import com.commerce.pagopa.identity.domain.*;
import com.commerce.pagopa.identity.event.UserProfileUpdated;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final ApplicationEventPublisher events;

    @Transactional
    public User register(UserCreateRequestDto requestDto) {
        User user = User.create(
                requestDto.provider(),
                requestDto.providerId(),
                requestDto.name(),
                requestDto.email(),
                requestDto.profileImageUrl(),
                LocalDateTime.now()
        );
        Role role = roleService.findUserRole();

        UserRole userRole = UserRole.create(
                user,
                role
        );
        user.addUserRole(userRole);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto find(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto update(Long userId, UserUpdateRequestDto requestDto) {
        User user = userRepository.findByIdOrThrow(userId);

        String oldProfileImageUrl = user.getProfileImageUrl();
        String newProfileImageUrl = requestDto.profileImageUrl();

        user.updateProfile(
                requestDto.name(),
                newProfileImageUrl
        );

        if (
            imageChanged(
                    newProfileImageUrl,
                    oldProfileImageUrl
            )
        ) {
            events.publishEvent(
                    new UserProfileUpdated(
                            UUID.randomUUID(),
                            requestDto.name(),
                            oldProfileImageUrl,
                            newProfileImageUrl,
                            user.getUpdatedAt()
                    )
            );
        }

        return UserResponseDto.from(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(Provider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(
                provider,
                providerId
        );
    }

    private static boolean imageChanged(
            String newProfileImageUrl,
            String oldProfileImageUrl
    ) {
        return StringUtils.hasText(newProfileImageUrl)
                && !Objects.equals(
                        oldProfileImageUrl,
                        newProfileImageUrl
                );
    }
}
