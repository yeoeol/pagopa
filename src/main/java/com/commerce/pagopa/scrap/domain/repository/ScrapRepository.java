package com.commerce.pagopa.scrap.domain.repository;

import com.commerce.pagopa.scrap.domain.model.Scrap;

import java.util.List;
import java.util.Optional;

/**
 * Scrap 도메인의 영속성 추상화. infrastructure 계층에서 구현.
 */
public interface ScrapRepository {
    Scrap save(Scrap scrap);

    Optional<Scrap> findById(Long id);

    void deleteById(Long id);

    List<Scrap> findAllByUserId(Long userId);
}
