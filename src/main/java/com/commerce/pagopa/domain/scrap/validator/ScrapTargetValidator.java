package com.commerce.pagopa.domain.scrap.validator;

import com.commerce.pagopa.domain.scrap.entity.enums.EntityType;

public interface ScrapTargetValidator {
    /**
     * 해당 검증기가 특정 EntityType을 지원하는지 확인합니다.
     */
    boolean supports(EntityType type);

    /**
     * 타겟의 존재 여부를 검증합니다. (존재하지 않으면 예외 발생)
     */
    void validate(Long targetId);
}