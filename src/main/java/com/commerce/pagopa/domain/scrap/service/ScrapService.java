package com.commerce.pagopa.domain.scrap.service;

import com.commerce.pagopa.domain.scrap.dto.request.ScrapAddRequestDto;
import com.commerce.pagopa.domain.scrap.dto.response.ScrapResponseDto;
import com.commerce.pagopa.domain.scrap.entity.Scrap;
import com.commerce.pagopa.domain.scrap.entity.enums.EntityType;
import com.commerce.pagopa.domain.scrap.repository.ScrapRepository;
import com.commerce.pagopa.domain.scrap.validator.ScrapTargetValidator;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.UserNotFoundException;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final List<ScrapTargetValidator> scrapTargetValidators;

    @Transactional
    public ScrapResponseDto addScrap(Long userId, ScrapAddRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        validateTarget(requestDto.targetType(), requestDto.targetId());

        Scrap scrap = Scrap.create(requestDto.targetId(), requestDto.targetType(), user);
        return ScrapResponseDto.from(scrapRepository.save(scrap));
    }

    private void validateTarget(EntityType type, Long targetId) {
        ScrapTargetValidator validator = scrapTargetValidators.stream()
                .filter(v -> v.supports(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SCRAP_TARGET_UNSUPPORTED));

        validator.validate(targetId);
    }

    @Transactional
    public void delete(Long scrapId) {
        scrapRepository.deleteById(scrapId);
    }

    @Transactional(readOnly = true)
    public List<ScrapResponseDto> findAllByUser(Long userId) {
        return scrapRepository.findAllByUserId(userId).stream()
                .map(ScrapResponseDto::from)
                .toList();
    }
}
