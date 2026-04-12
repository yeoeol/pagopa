package com.commerce.pagopa.domain.scrap.validator;

import com.commerce.pagopa.domain.scrap.entity.Scrap;
import com.commerce.pagopa.domain.scrap.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("scrapOwnerValidator")
@RequiredArgsConstructor
public class ScrapOwnerValidator {

    private final ScrapRepository scrapRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long scrapId, Long userId) {
        if (scrapId == null || userId == null) {
            return false;
        }
        Scrap scrap = scrapRepository.findById(scrapId).orElse(null);
        if (scrap == null || scrap.getUser() == null) {
            return false;
        }
        return scrap.getUser().getId().equals(userId);
    }
}
