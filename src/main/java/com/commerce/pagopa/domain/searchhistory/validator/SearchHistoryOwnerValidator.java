package com.commerce.pagopa.domain.searchhistory.validator;

import com.commerce.pagopa.domain.searchhistory.entity.SearchHistory;
import com.commerce.pagopa.domain.searchhistory.repository.SearchHistoryRepository;
import com.commerce.pagopa.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("searchHistoryOwnerValidator")
@RequiredArgsConstructor
public class SearchHistoryOwnerValidator {

    private final SearchHistoryRepository searchHistoryRepository;
    private final HttpServletRequest request;

    @Transactional(readOnly = true)
    public boolean isOwner(Long searchHistoryId, Long userId) {
        if (searchHistoryId == null) {
            return false;
        }

        SearchHistory searchHistory = searchHistoryRepository.findById(searchHistoryId).orElse(null);
        if (searchHistory == null) {
            return false; // 이미 삭제됐거나 존재하지 않음
        }

        // 로그인한 회원인 경우 (userId 비교)
        if (userId != null && searchHistory.getUser() != null) {
            return searchHistory.getUser().getId().equals(userId);
        }

        // 비로그인 사용자(세션 ID)인 경우 쿠키 값을 확인
        if (userId == null && searchHistory.getUser() == null && searchHistory.getSessionId() != null) {
            String sessionId = CookieUtil.getSessionIdFromCookie(request);
            return searchHistory.getSessionId().equals(sessionId);
        }

        // 그 외의 경우는 본인의 검색 기록이 아님
        return false;
    }
}