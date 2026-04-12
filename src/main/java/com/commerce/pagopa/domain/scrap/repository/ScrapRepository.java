package com.commerce.pagopa.domain.scrap.repository;

import com.commerce.pagopa.domain.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    List<Scrap> findAllByUserId(Long userId);
}
