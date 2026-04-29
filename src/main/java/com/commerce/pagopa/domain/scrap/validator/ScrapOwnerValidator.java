package com.commerce.pagopa.domain.scrap.validator;

import com.commerce.pagopa.domain.scrap.entity.Scrap;
import com.commerce.pagopa.domain.scrap.repository.ScrapRepository;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("scrapOwnerValidator")
@RequiredArgsConstructor
public class ScrapOwnerValidator extends OwnerValidator<Scrap, Long> {

    private final ScrapRepository scrapRepository;

    @Override
    protected Optional<Scrap> findResource(Long scrapId) {
        return scrapRepository.findById(scrapId);
    }

    @Override
    protected Long extractOwnerId(Scrap scrap) {
        return scrap.getUser() == null ? null : scrap.getUser().getId();
    }
}
