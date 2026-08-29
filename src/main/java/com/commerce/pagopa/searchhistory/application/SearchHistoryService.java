package com.commerce.pagopa.searchhistory.application;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.searchhistory.application.dto.response.SearchHistoryResponseDto;
import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveHistory(Long userId, String sessionId, String keyword) {
        Instant now = Instant.now();

        String normalizeKeyword = normalizeKeyword(keyword);
        if (normalizeKeyword == null) {
            return;
        }

        // 로그인 회원
        if (userId != null) {
            if (!userRepository.existsById(userId)) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            Optional<SearchHistory> existingHistory = searchHistoryRepository
                            .findByUserIdAndKeywordForUpdate(userId, keyword);

            existingHistory.ifPresentOrElse(
                    sh -> sh.updateLastSearchedAt(now),
                    () -> searchHistoryRepository.save(
                            SearchHistory.createForUser(userId, keyword, now)
                    )
            );
        }
        // 비로그인 사용자 (세션 기반)
        else if (hasText(sessionId)) {
            Optional<SearchHistory> existingHistory = searchHistoryRepository
                    .findBySessionIdAndKeywordForUpdate(sessionId, keyword);

            existingHistory.ifPresentOrElse(
                    sh -> sh.updateLastSearchedAt(now),
                    () -> searchHistoryRepository.save(
                            SearchHistory.createForGuest(sessionId, keyword, now)
                    )
            );
        }
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDto> getHistories(Long userId, String sessionId) {
        if (userId != null) {
            return searchHistoryRepository
                    .findByUserIdOrderByLastSearchedAtDesc(userId)
                    .stream()
                    .map(SearchHistoryResponseDto::from)
                    .toList();
        } else if (hasText(sessionId)) {
            return searchHistoryRepository
                    .findBySessionIdOrderByLastSearchedAtDesc(sessionId)
                    .stream()
                    .map(SearchHistoryResponseDto::from)
                    .toList();
        }
        return List.of();
    }

    @Transactional
    public void delete(Long searchHistoryId, Long userId, String sessionId) {
        if (userId != null) {
            searchHistoryRepository.deleteByIdAndUserId(searchHistoryId, userId);
        } else if (hasText(sessionId)) {
            searchHistoryRepository.deleteByIdAndSessionId(searchHistoryId, sessionId);
        }
    }

    @Transactional
    public void deleteAll(Long userId, String sessionId) {
        if (userId != null) {
            searchHistoryRepository.deleteByUserId(userId);
        } else if (hasText(sessionId)) {
            searchHistoryRepository.deleteBySessionId(sessionId);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (!hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }
}
