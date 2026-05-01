package com.commerce.pagopa.auth.service;

import com.commerce.pagopa.user.domain.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void logout_deletesRefreshTokenByUserId() {
        authService.logout(1L);

        verify(refreshTokenRepository).deleteByUserId(1L);
    }
}
