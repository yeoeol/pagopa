package com.commerce.pagopa.discovery.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.discovery.application.command.ProductSearchHistoryCommand;
import com.commerce.pagopa.discovery.application.dto.response.SearchHistoryResponseDto;
import com.commerce.pagopa.discovery.domain.SearchHistoryRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.identity.api.UserApi;

import static com.commerce.pagopa.global.util.StringUtil.normalize;
import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserApi userApi;

    @Transactional
    public void saveHistory(ProductSearchHistoryCommand command) {
        Long userId = command.userId();
        String sessionId = command.sessionId();
        LocalDateTime searchedAt = command.searchedAt();

        String normalizeKeyword = normalize(command.keyword());
        if (normalizeKeyword == null) {
            return;
        }

        // 로그인 회원
        if (userId != null) {
            if (!userApi.existsById(userId)) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            searchHistoryRepository.upsertByUserId(
                    userId,
                    normalizeKeyword,
                    searchedAt
            );
        }
        // 비로그인 사용자 (세션 기반)
        else if (hasText(sessionId)) {
            searchHistoryRepository.upsertBySessionId(
                    sessionId,
                    normalizeKeyword,
                    searchedAt
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
        } else if (hasText(sessionId)) {
            return searchHistoryRepository.findBySessionIdOrderByLastSearchedAtDesc(sessionId)
                    .stream()
                    .map(SearchHistoryResponseDto::from)
                    .toList();
        }
        return List.of();
    }

    @Transactional
    public void delete(Long searchHistoryId, Long userId, String sessionId) {
        if (userId != null) {
            searchHistoryRepository.deleteByIdAndUserId(
                    searchHistoryId,
                    userId
            );
        } else if (hasText(sessionId)) {
            searchHistoryRepository.deleteByIdAndSessionId(
                    searchHistoryId,
                    sessionId
            );
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
}
