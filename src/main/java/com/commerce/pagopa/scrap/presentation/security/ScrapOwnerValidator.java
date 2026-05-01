package com.commerce.pagopa.scrap.presentation.security;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.scrap.domain.model.Scrap;
import com.commerce.pagopa.scrap.domain.repository.ScrapRepository;
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
        return Optional.ofNullable(scrap.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
