package com.commerce.pagopa.catalog.application;

import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.catalog.domain.CategoryRepository;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;
import com.commerce.pagopa.catalog.domain.ProductStatus;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.support.fixture.CategoryFixture;
import com.commerce.pagopa.support.fixture.ProductFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ProductApiServiceTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long SELLER_ID = 10L;

    private ProductRepository productRepository;
    private ProductApiService productApiService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        // default 메서드는 mock이 null을 반환하므로 실제 구현(findByIdForUpdate + orElseThrow)을 타게 한다
        given(productRepository.findByIdForUpdateOrThrow(any())).willCallRealMethod();
        productApiService = new ProductApiService(productRepository, mock(CategoryRepository.class));
    }

    @Test
    @DisplayName("상품을 숨기면 상태가 HIDDEN으로 바뀌고 반환된 ProductSummary에 반영된다")
    void hide_activeProduct_changesStatusToHidden() {
        Product product = product();
        given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(Optional.of(product));

        ProductSummary result = productApiService.hide(PRODUCT_ID);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.status()).isEqualTo(ProductStatus.HIDDEN.name());
    }

    @Test
    @DisplayName("존재하지 않는 상품이면 PRODUCT_NOT_FOUND 예외를 던진다")
    void hide_productNotFound_throwsBusinessException() {
        given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productApiService.hide(PRODUCT_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 HIDDEN인 상품을 다시 숨기면 PRODUCT_ALREADY_HIDDEN 예외를 던진다")
    void hide_alreadyHidden_throwsBusinessException() {
        Product product = product();
        product.hide();
        given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(Optional.of(product));

        assertThatThrownBy(() -> productApiService.hide(PRODUCT_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_ALREADY_HIDDEN);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    private Product product() {
        Product product = ProductFixture.aProduct(CategoryFixture.aRoot(), SELLER_ID);
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
        return product;
    }
}
