package com.commerce.pagopa.scrap.application.port;

import com.commerce.pagopa.scrap.domain.model.EntityType;

/**
 * 다른 도메인의 엔티티가 스크랩 타겟으로 유효한지 검증하는 외부 포트.
 * 각 도메인에서 구현체를 제공 (예: ProductScrapValidator).
 */
public interface ScrapTargetValidator {
    boolean supports(EntityType type);

    void validate(Long targetId);
}
