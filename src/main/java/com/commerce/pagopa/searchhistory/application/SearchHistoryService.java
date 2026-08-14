package com.commerce.pagopa.searchhistory.application;

import com.commerce.pagopa.searchhistory.application.dto.response.SearchHistoryResponseDto;
import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveHistory(Long userId, String sessionId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }

        // 로그인 사용자
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                Optional<SearchHistory> existingHistory = searchHistoryRepository.findByUserIdAndKeyword(userId, keyword);
                existingHistory.ifPresentOrElse(
						SearchHistory::updateLastSearchedAt,
                        () -> searchHistoryRepository.save(SearchHistory.createForUser(user, keyword))
                );
            }
        }
        // 비로그인 사용자 (세션 기반)
        else if (sessionId != null && !sessionId.isBlank()) {
            Optional<SearchHistory> existingHistory = searchHistoryRepository.findBySessionIdAndKeyword(sessionId, keyword);
            existingHistory.ifPresentOrElse(
                    SearchHistory::updateLastSearchedAt,
                    () -> searchHistoryRepository.save(SearchHistory.createForGuest(sessionId, keyword))
            );
        }
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDto> getHistories(Long userId, String sessionId) {
        if (userId != null) {
            return searchHistoryRepository.findByUserIdOrderByLastSearchedAtDesc(userId)
                    .stream()
                    .map(SearchHistoryResponseDto::from)
                    .toList();
        } else if (sessionId != null && !sessionId.isBlank()) {
            return searchHistoryRepository.findBySessionIdOrderByLastSearchedAtDesc(sessionId)
                    .stream()
                    .map(SearchHistoryResponseDto::from)
                    .toList();
        }

        return List.of();
    }

    @Transactional
    public void delete(Long searchHistoryId) {
        searchHistoryRepository.deleteById(searchHistoryId);
    }

    @Transactional
    public void deleteAll(Long userId, String sessionId) {
        if (userId != null) {
            searchHistoryRepository.deleteByUserId(userId);
        } else if (sessionId != null && !sessionId.isBlank()) {
            searchHistoryRepository.deleteBySessionId(sessionId);
        }
    }
}
