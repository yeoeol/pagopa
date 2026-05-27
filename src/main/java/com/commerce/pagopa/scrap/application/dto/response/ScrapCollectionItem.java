package com.commerce.pagopa.scrap.application.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProductScrapDto.class, name = "Product"),
    // @JsonSubTypes.Type(value = BrandScrapDto.class, name = "BRAND")
})
public interface ScrapCollectionItem {
    Long collectionId();
    Long id();
}
