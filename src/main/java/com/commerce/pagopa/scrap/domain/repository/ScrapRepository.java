package com.commerce.pagopa.scrap.domain.repository;

import com.commerce.pagopa.scrap.domain.model.Scrap;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository {
    Scrap save(Scrap scrap);

    Optional<Scrap> findById(Long id);

    void deleteById(Long id);

    List<Scrap> findAllByUserId(Long userId);

    long countByUserId(Long userId);
}
