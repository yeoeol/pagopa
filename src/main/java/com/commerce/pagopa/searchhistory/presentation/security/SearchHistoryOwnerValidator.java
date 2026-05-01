package com.commerce.pagopa.searchhistory.presentation.security;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.util.CookieUtil;
import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component("searchHistoryOwnerValidator")
@RequiredArgsConstructor
public class SearchHistoryOwnerValidator extends OwnerValidator<SearchHistory, Long> {

    private final SearchHistoryRepository searchHistoryRepository;
    private final HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(Long searchHistoryId, Long userId) {
        if (searchHistoryId == null) {
            return false;
        }

        return findResource(searchHistoryId).map(history -> {
            // 로그인 회원 검증
            Long ownerId = extractOwnerId(history);
            if (userId != null && ownerId != null) {
                return ownerId.equals(userId);
            }

            // 비로그인 사용자 검증: 세션 ID 비교
            if (userId == null && history.getUser() == null && history.getSessionId() != null) {
                String sessionId = CookieUtil.getSessionIdFromCookie(request);
                return history.getSessionId().equals(sessionId);
            }
            return false;
        }).orElse(false);
    }

    @Override
    protected Optional<SearchHistory> findResource(Long searchHistoryId) {
        return searchHistoryRepository.findById(searchHistoryId);
    }

    @Override
    protected Long extractOwnerId(SearchHistory searchHistory) {
        return Optional.ofNullable(searchHistory.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
