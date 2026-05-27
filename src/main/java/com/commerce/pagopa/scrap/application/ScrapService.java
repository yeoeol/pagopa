package com.commerce.pagopa.scrap.application;

import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.scrap.application.dto.response.ProductScrapDto;
import com.commerce.pagopa.scrap.application.dto.response.ScrapCollectionItem;
import com.commerce.pagopa.scrap.application.dto.response.ScrapListResponseDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.scrap.application.dto.request.ScrapAddRequestDto;
import com.commerce.pagopa.scrap.application.dto.response.ScrapResponseDto;
import com.commerce.pagopa.scrap.application.port.ScrapTargetValidator;
import com.commerce.pagopa.scrap.domain.model.EntityType;
import com.commerce.pagopa.scrap.domain.model.Scrap;
import com.commerce.pagopa.scrap.domain.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final List<ScrapTargetValidator> scrapTargetValidators;

    @Transactional
    public ScrapResponseDto addScrap(Long userId, ScrapAddRequestDto requestDto) {
        User user = userRepository.findByIdOrThrow(userId);

        validateTarget(requestDto.targetType(), requestDto.targetId());

        Scrap scrap = Scrap.create(requestDto.targetId(), requestDto.targetType(), user);
        return ScrapResponseDto.from(scrapRepository.save(scrap));
    }

    private void validateTarget(EntityType type, Long targetId) {
        ScrapTargetValidator validator = scrapTargetValidators.stream()
                .filter(v -> v.supports(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SCRAP_TARGET_UNSUPPORTED));

        validator.validate(targetId);
    }

    @Transactional
    public void delete(Long scrapId) {
        scrapRepository.deleteById(scrapId);
    }

    @Transactional(readOnly = true)
    public ScrapListResponseDto findAllByUser(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        List<Scrap> scraps = scrapRepository.findAllByUserId(userId);

        Map<EntityType, List<Long>> targetIdsByType = scraps.stream()
                .collect(groupingBy(
                        Scrap::getTargetType,
                        mapping(Scrap::getTargetId, toList())
                ));

        Map<String, Integer> count = new HashMap<>();
        List<ScrapCollectionItem> collectionItems = new ArrayList<>();

        for (EntityType entityType : EntityType.values()) {
            List<Long> ids = targetIdsByType.getOrDefault(entityType, Collections.emptyList());
            if (entityType.equals(EntityType.PRODUCT)) {
                List<Product> productScraps = productRepository.findAllByIdIn(ids);
                count.put(entityType.getDescription(), productScraps.size());
                for (Product productScrap : productScraps) {
                    collectionItems.add(ProductScrapDto.from(productScrap.getId(), productScrap));
                }
            }
        }

        return new ScrapListResponseDto(
                count,
                UserResponseDto.from(user),
                collectionItems
        );
    }

    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        return scrapRepository.countByUserId(userId);
    }
}
