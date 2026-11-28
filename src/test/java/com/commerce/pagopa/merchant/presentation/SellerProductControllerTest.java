package com.commerce.pagopa.merchant.presentation;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.infrastructure.security.CustomUserDetails;
import com.commerce.pagopa.merchant.application.SellerProductService;
import com.commerce.pagopa.merchant.presentation.security.SellerProductOwnerValidator;
import com.commerce.pagopa.support.ControllerSliceTest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerSliceTest(SellerProductController.class)
@Import(SellerProductControllerTest.MethodSecurityConfig.class)
class SellerProductControllerTest {

    // @EnableMethodSecurity가 붙은 SecurityConfig는 슬라이스에 로드되지 않으므로 @PreAuthorize 검증을 위해 켠다.
    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final String HIDE_URL = "/api/v1/sellers/products/{productId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerProductService sellerProductService;

    // @PreAuthorize("@sellerProductOwnerValidator.isOwner(...)")가 참조하는 빈. 슬라이스 테스트에는 로드되지 않아 mock으로 등록한다.
    @MockitoBean(name = "sellerProductOwnerValidator")
    private SellerProductOwnerValidator sellerProductOwnerValidator;

    // ControllerSliceTest는 addFilters=false라 보안 필터가 SecurityContext를 채우지 않으므로 직접 설정한다.
    @BeforeEach
    void setUpAuthentication() {
        CustomUserDetails principal = new CustomUserDetails(
                USER_ID,
                "seller@test.com",
                Set.of(RoleCode.ROLE_SELLER)
        );
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        )
                );
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("상품 숨김 성공 시 200과 HIDDEN 상태의 상품을 반환한다")
    void hide_success() throws Exception {
        given(sellerProductOwnerValidator.isOwner(PRODUCT_ID, USER_ID)).willReturn(true);
        given(sellerProductService.hide(PRODUCT_ID)).willReturn(
                new ProductSummary(
                        PRODUCT_ID,
                        "상품",
                        "설명",
                        10000,
                        5,
                        "HIDDEN",
                        3L,
                        7L
                )
        );

        mockMvc.perform(patch(HIDE_URL, PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(sellerProductService).hide(PRODUCT_ID);
    }

    @Test
    @DisplayName("상품을 등록한 판매자가 아니면 403을 반환하고 서비스를 호출하지 않는다")
    void hide_notOwner_forbidden() throws Exception {
        given(sellerProductOwnerValidator.isOwner(PRODUCT_ID, USER_ID)).willReturn(false);

        mockMvc.perform(patch(HIDE_URL, PRODUCT_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ACCESS_DENIED.getCode()));

        verifyNoInteractions(sellerProductService);
    }

    @Test
    @DisplayName("이미 숨김 처리된 상품이면 409를 반환한다")
    void hide_alreadyHidden_conflict() throws Exception {
        given(sellerProductOwnerValidator.isOwner(PRODUCT_ID, USER_ID)).willReturn(true);
        given(sellerProductService.hide(PRODUCT_ID)).willThrow(
                new BusinessException(ErrorCode.PRODUCT_ALREADY_HIDDEN)
        );

        mockMvc.perform(patch(HIDE_URL, PRODUCT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.PRODUCT_ALREADY_HIDDEN.getCode()));
    }

    @Test
    @DisplayName("상품이 없으면 404를 반환하고 서비스를 호출하지 않는다")
    void hide_productNotFound() throws Exception {
        // 실제 SellerProductOwnerValidator는 productApi.get()에서 PRODUCT_NOT_FOUND를 던진다.
        given(sellerProductOwnerValidator.isOwner(PRODUCT_ID, USER_ID)).willThrow(
                new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        );

        mockMvc.perform(patch(HIDE_URL, PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.PRODUCT_NOT_FOUND.getCode()));

        verifyNoInteractions(sellerProductService);
    }

    @ParameterizedTest(name = "productId={0}")
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("0 이하의 id면 400을 반환하고 서비스를 호출하지 않는다")
    void hide_nonPositiveId_badRequest(String productId) throws Exception {
        mockMvc.perform(patch(HIDE_URL, productId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));

        verifyNoInteractions(sellerProductService);
    }

    @ParameterizedTest(name = "productId={0}")
    @ValueSource(strings = {"abc", "1.5", "99999999999999999999"})
    @DisplayName("숫자(Long)로 변환할 수 없는 id면 400을 반환하고 서비스를 호출하지 않는다")
    void hide_invalidTypeId_badRequest(String productId) throws Exception {
        mockMvc.perform(patch(HIDE_URL, productId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_TYPE_VALUE.getCode()));

        verifyNoInteractions(sellerProductService);
    }
}
