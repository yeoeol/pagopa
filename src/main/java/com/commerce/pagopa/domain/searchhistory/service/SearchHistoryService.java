package com.commerce.pagopa.domain.searchhistory.service;

import com.commerce.pagopa.domain.searchhistory.dto.response.SearchHistoryResponseDto;
import com.commerce.pagopa.domain.searchhistory.entity.SearchHistory;
import com.commerce.pagopa.domain.searchhistory.repository.SearchHistoryRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                SearchHistory history = SearchHistory.createForUser(user, keyword);
                searchHistoryRepository.save(history);
            }
        } 
        // 비로그인 사용자 (세션 기반)
        else if (sessionId != null && !sessionId.isBlank()) {
            SearchHistory history = SearchHistory.createForGuest(sessionId, keyword);
            searchHistoryRepository.save(history);
        }
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDto> getHistories(Long userId, String sessionId) {
        if (userId != null) {
            return searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(SearchHistoryResponseDto::from)
                    .toList();
        } else if (sessionId != null && !sessionId.isBlank()) {
            return searchHistoryRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
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