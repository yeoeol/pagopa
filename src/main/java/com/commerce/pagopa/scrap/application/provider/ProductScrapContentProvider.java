package com.commerce.pagopa.scrap.application.provider;

import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.scrap.application.dto.response.ProductScrapDto;
import com.commerce.pagopa.scrap.application.dto.response.ScrapCollectionItem;
import com.commerce.pagopa.scrap.application.port.ScrapContentProvider;
import com.commerce.pagopa.scrap.domain.model.EntityType;
import com.commerce.pagopa.scrap.domain.model.Scrap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductScrapContentProvider implements ScrapContentProvider {

    private final ProductRepository productRepository;

    @Override
    public boolean supports(EntityType type) {
        return type == EntityType.PRODUCT;
    }

    @Override
    public List<ScrapCollectionItem> fetchItems(List<Scrap> scraps) {
        List<Long> productIds = scraps.stream()
                .map(Scrap::getTargetId)
                .toList();

        Map<Long, Product> productsById = productRepository.findAllByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return scraps.stream()
                .map(scrap -> {
                    Product product = productsById.get(scrap.getTargetId());
                    if (product == null) {
                        return null;
                    }

                    return ProductScrapDto.from(scrap.getId(), product);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
}
