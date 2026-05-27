package com.commerce.pagopa.scrap.application.port;

import com.commerce.pagopa.scrap.application.dto.response.ScrapCollectionItem;
import com.commerce.pagopa.scrap.domain.model.EntityType;
import com.commerce.pagopa.scrap.domain.model.Scrap;

import java.util.List;

/**
 * 특정 타입(EntityType)의 스크랩 목록에 대한 상세 컨텐츠를 조회하고,
 * 공통 응답 DTO(ScrapCollectionItem)로 변환하는 역할을 정의합니다.
 */
public interface ScrapContentProvider {

    boolean supports(EntityType type);

    /**
     * 주어진 스크랩 목록(Scraps)에 대한 상세 정보를 조회하고 DTO 목록으로 변환합니다.
     * @param scraps 동일한 EntityType을 가진 스크랩 목록
     * @return 상세 정보가 채워진 DTO 목록
     */
    List<ScrapCollectionItem> fetchItems(List<Scrap> scraps);
}
