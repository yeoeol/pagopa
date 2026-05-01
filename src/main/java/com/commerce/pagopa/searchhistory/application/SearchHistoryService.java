package com.commerce.pagopa.searchhistory.application;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.searchhistory.application.dto.response.SearchHistoryResponseDto;
import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
                if (existingHistory.isPresent()) {
                    existingHistory.get().updateLastSearchedAt(); // 이미 존재하면 갱신만 수행
                } else {
                    SearchHistory history = SearchHistory.createForUser(user, keyword);
                    searchHistoryRepository.save(history);
                }
            }
        }
        // 비로그인 사용자 (세션 기반)
        else if (sessionId != null && !sessionId.isBlank()) {
            Optional<SearchHistory> existingHistory = searchHistoryRepository.findBySessionIdAndKeyword(sessionId, keyword);
            if (existingHistory.isPresent()) {
                existingHistory.get().updateLastSearchedAt(); // 이미 존재하면 갱신만 수행
            } else {
                SearchHistory history = SearchHistory.createForGuest(sessionId, keyword);
                searchHistoryRepository.save(history);
            }
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
