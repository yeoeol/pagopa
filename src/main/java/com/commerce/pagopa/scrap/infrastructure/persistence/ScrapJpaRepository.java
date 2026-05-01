package com.commerce.pagopa.scrap.infrastructure.persistence;

import com.commerce.pagopa.scrap.domain.model.Scrap;
import com.commerce.pagopa.scrap.domain.repository.ScrapRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapJpaRepository extends JpaRepository<Scrap, Long>, ScrapRepository {

    @Override
    List<Scrap> findAllByUserId(Long userId);
}
