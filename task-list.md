# task-list.md
> Planner Agent가 관리. 상태: 🔲 대기 / 🔄 진행중 / ✅ 완료 / ❌ 블로킹

## 현재 스프린트: 삭제 처리 및 위치 수정 (assistant / merchant / media)
- 목표:
  - `DELETE /api/v1/assistant/conversations/{conversationId}/messages/{messageId}` — 본인 대화방의 본인(USER) 메시지 하드 삭제
  - `PATCH /api/v1/sellers/products/{productId}` — 상품을 등록한 판매자만 상품 상태를 HIDDEN으로 변경
  - `ImageCategory`를 `media.domain` → `media.api`로 이동
- 기한: 미정
- 확정된 결정 (2026-11-28 사용자 답변):
  - 메시지 삭제 방식: **하드 삭제**. "이미 삭제된 메시지 → 409" 요구사항은 **철회**하고, 재삭제 요청은 `ASSISTANT_MESSAGE_NOT_FOUND`(404)로 응답 (옵션 b)
  - "본인 메시지" 범위: **본인 대화방 + USER 역할 메시지만** (ASSISTANT 메시지는 삭제 불가)
  - **닫힌(CLOSED) 대화방의 메시지는 삭제 불가**
  - **다른 대화방 소속 메시지 삭제 요청은 403** (USER가 아닌 메시지와 같은 "본인 메시지 아님" 에러코드)
  - **이미 HIDDEN인 상품을 다시 숨기면 409**
  - 상품 PATCH 요청 바디: **없음** (호출하면 HIDDEN으로 변경)
  - ImageCategory 이동 위치: **`com.commerce.pagopa.media.api`**
- 엔드포인트 구현 순서 (Outside-in, 테스트 먼저):
  1. `*ControllerTest`(MockMvc, Service는 `@MockitoBean`) 작성 → 엔드포인트가 없으니 실패(red) 확인
  2. Controller 메서드 추가 + Service 메서드 시그니처만 추가 (본문은 `throw new UnsupportedOperationException()`) → Controller 테스트 통과(green)
  3. `*ServiceTest` 작성 → 시그니처만 있어서 실패(red) 확인
  4. Service/Repository/도메인 구현 → 통과(green)
- 현재 코드에서 확인한 사실:
  - `/api/v1/assistant/**`는 `authenticated()`, `/api/v1/sellers/**`는 `hasRole("SELLER")` (`identity/infrastructure/security/config/SecurityConfig.java`). 401은 기존 `ApiAuthenticationEntryPoint` + `ErrorCode.UNAUTHORIZED / INVALID_TOKEN / EXPIRED_TOKEN`이 처리
  - `@ControllerSliceTest`는 `addFilters = false`라서 슬라이스 테스트로는 401을 검증할 수 없음 → 별도 태스크(TASK-006)로 분리
  - merchant 모듈은 `catalog :: api`만 참조할 수 있음 (`merchant/package-info.java`) → 상품 상태 변경은 catalog의 `ProductApi`를 거쳐야 함
  - 기존 판매자 상품 API의 응답 DTO는 `catalog.api.ProductSummary`(record), 소유자 검증은 `@PreAuthorize("@sellerProductOwnerValidator.isOwner(#productId, principal.userId)")`
  - 닫힌 대화방에 대한 기존 에러코드: `ASSISTANT_CONVERSATION_CLOSED`(409, `ASSISTANT_004`) — `AssistantHistoryService.saveUserMessage`에서 이미 사용 중
  - `ImageCategory`를 쓰는 곳: `media/api/ImageUploadRequest`, `media/infrastructure/azure/AzureImageService`, `media/infrastructure/supabase/SupabaseImageService`, `media/presentation/ImageApiController` (테스트 코드에는 없음)

## 태스크 목록

### TASK-001: ImageCategory 패키지를 media.domain → media.api로 이동
- 상태: ✅
- 복잡도: LOW
- 의존: 없음
- verify: `./gradlew test --tests "com.commerce.pagopa.media.infrastructure.supabase.SupabaseImageServiceTest"` + `./gradlew test --tests "com.commerce.pagopa.ApplicationModulithTest"`
- 구현 범위:
  - src/main/java/com/commerce/pagopa/media/domain/ImageCategory.java → src/main/java/com/commerce/pagopa/media/api/ImageCategory.java (package 선언 변경, 내용 그대로)
  - src/main/java/com/commerce/pagopa/media/api/ImageUploadRequest.java (import 제거 — 같은 패키지가 됨)
  - src/main/java/com/commerce/pagopa/media/infrastructure/azure/AzureImageService.java (import 경로)
  - src/main/java/com/commerce/pagopa/media/infrastructure/supabase/SupabaseImageService.java (import 경로)
  - src/main/java/com/commerce/pagopa/media/presentation/ImageApiController.java (import 경로)
- 완료 기준:
  - [x] `media.domain`에서 `ImageCategory`를 참조하는 곳이 하나도 없음 (grep)
  - [x] `./gradlew compileTestJava` 통과
  - [x] SupabaseImageServiceTest, ApplicationModulithTest 통과
  - [x] Critic APPROVE
- 메모:
  - enum 상수 이름(PROFILE/PRODUCT/REVIEW)과 `directory` 값은 그대로 둔다 → `ImageApiController`의 `{category}` path 바인딩과 pagopa-web 호출부에는 영향 없음
  - `media/domain/` 패키지에 다른 클래스가 없어 빈 디렉터리가 남으면 삭제 여부를 보고만 한다 (CLAUDE.md §4)
  - `AzureImageServiceIntegrationTest`는 외부 스토리지 연동이라 verify 대상에서 뺐다. 3단계 전체 테스트에서 실패하면 원인만 보고

### TASK-002: 메시지 삭제 — Controller DELETE 엔드포인트 + ControllerTest (테스트 먼저)
- 상태: ✅
- 복잡도: LOW
- 의존: 없음
- verify: `./gradlew test --tests "com.commerce.pagopa.assistant.chat.presentation.AssistantControllerTest"`
- 구현 범위:
  - src/test/java/com/commerce/pagopa/assistant/chat/presentation/AssistantControllerTest.java (**먼저** 케이스 추가 → red 확인)
  - src/main/java/com/commerce/pagopa/assistant/chat/presentation/AssistantController.java (`@DeleteMapping("/{conversationId}/messages/{messageId}")` 추가)
  - src/main/java/com/commerce/pagopa/assistant/chat/application/AssistantHistoryService.java (`delete(userId, conversationId, messageId)` **시그니처만**, 본문은 `UnsupportedOperationException` — TASK-003에서 구현)
- 완료 기준:
  - [x] 엔드포인트를 추가하기 전에 테스트가 실패(red)하는 것을 확인하고 기록
  - [x] 성공 시 200 + `ApiResponse.ok()` → `{"success": true}` (data 생략, `@JsonInclude(NON_NULL)`)
  - [x] 실패 시 `@RestControllerAdvice`가 `ApiResponse.error(...)` → `{"success": false, "error": {...}}` — 응답은 성공/실패 두 형태뿐
  - [x] 입력 검증: `conversationId`, `messageId`에 `@Positive` → 0, -1은 400 / "abc"처럼 타입이 안 맞으면 400 (기존 `close_nonPositiveId_badRequest`, `close_invalidTypeId_badRequest` 패턴 재사용)
  - [x] Service mock이 던지는 예외별 응답 검증: 404(대화방/메시지 없음), 403(본인 메시지 아님), 409(닫힌 대화방)
  - [x] Critic APPROVE
- 메모:
  - DELETE는 요청 바디가 없어서 "요청 DTO 검증"은 path variable 검증으로 대신한다 → 블로킹 이슈 #1 참고
  - 401은 이 슬라이스 테스트로 검증할 수 없음(`addFilters = false`) → TASK-006
  - 403에 쓸 에러코드(예: `ASSISTANT_MESSAGE_NOT_OWNER`)는 이 태스크에서 `ErrorCode`에 추가한다. 기존 `NOT_PRODUCT_OWNER`(403)와 같은 형태
  - [Coder 기록 2026-11-28] red 확인: `@DeleteMapping` 추가 전 신규 14케이스 전부 FAILED (매핑 없음 → 기대 200/400, 실제 404), 기존 8케이스는 통과. 매핑 추가 후 22/22 통과
  - [Coder 기록] 추가한 에러코드: `ASSISTANT_MESSAGE_NOT_OWNER`(403, `ASSISTANT_006`)

### TASK-003: 메시지 삭제 — Service·Repository 구현 + ServiceTest (테스트 먼저)
- 상태: ✅
- 복잡도: MEDIUM
- 의존: TASK-002
- verify: `./gradlew test --tests "com.commerce.pagopa.assistant.chat.application.AssistantHistoryServiceTest"`
- 구현 범위:
  - src/test/java/com/commerce/pagopa/assistant/chat/application/AssistantHistoryServiceTest.java (신규, **먼저** 작성 → red 확인)
  - src/main/java/com/commerce/pagopa/assistant/chat/domain/AssistantMessageRepository.java (삭제 메서드 선언 — JPA 구현은 `AssistantMessageJpaRepository`가 `JpaRepository`에서 상속)
  - src/main/java/com/commerce/pagopa/assistant/chat/application/AssistantHistoryService.java (`delete` 본문 구현)
- 완료 기준:
  - [x] 본인 대화방(`findByIdAndUserId`)이 아니면 `ASSISTANT_CONVERSATION_NOT_FOUND`(404)
  - [x] 대화방이 CLOSED면 `ASSISTANT_CONVERSATION_CLOSED`(409)
  - [x] 메시지가 없으면(이미 삭제된 경우 포함) `ASSISTANT_MESSAGE_NOT_FOUND`(404)
  - [x] 메시지가 다른 대화방 소속이면 403 (TASK-002에서 추가한 "본인 메시지 아님" 에러코드)
  - [x] 메시지 role이 ASSISTANT면 403 (같은 에러코드)
  - [x] 정상 케이스에서 repository 삭제 호출 검증
  - [x] Critic APPROVE
- 메모:
  - 검사 순서: 대화방 소유 → CLOSED 여부 → 메시지 존재 → 대화방 소속 → role
  - [Coder 기록 2026-11-28] red 확인: (1) Repository `delete` 선언 전 컴파일 실패 (2) 선언 후 `UnsupportedOperationException`으로 6/6 FAILED. 구현 후 6/6 통과
  - [Coder 기록] Mockito mock은 인터페이스 default 메서드(`findByIdOrThrow`)를 실행하지 않아 테스트에서 `willCallRealMethod()`로 설정함
  - [Coder 기록] 동시성: 대화방 조회를 `findByIdAndUserIdForUpdate`(비관적 쓰기 락)로 해서 종료(close)와 직렬화함 — `saveUserMessage`와 같은 방식. `AssistantConversationConcurrencyTest.delete_waitsForClose_thenFailsWithClosed` 추가: 락 없는 조회로 돌렸을 때 FAILED(red) 확인 후 락 조회로 통과. 전체 122 tests / 0 fail

### TASK-004: 상품 숨김 — Controller PATCH 엔드포인트 + ControllerTest (테스트 먼저)
- 상태: 🔄
- 복잡도: MEDIUM
- 의존: 없음
- verify: `./gradlew test --tests "com.commerce.pagopa.merchant.presentation.SellerProductControllerTest"`
- 구현 범위:
  - src/test/java/com/commerce/pagopa/merchant/presentation/SellerProductControllerTest.java (신규, **먼저** 작성 → red 확인)
  - src/main/java/com/commerce/pagopa/merchant/presentation/SellerProductController.java (`@PatchMapping("/{productId}")` + 기존 `@PreAuthorize("@sellerProductOwnerValidator.isOwner(#productId, principal.userId)")` 재사용, 요청 바디 없음)
  - src/main/java/com/commerce/pagopa/merchant/application/SellerProductService.java (`hide(productId)` **시그니처만** — TASK-005에서 구현)
- 완료 기준:
  - [ ] 엔드포인트를 추가하기 전에 테스트가 실패(red)하는 것을 확인하고 기록
  - [ ] 성공 시 200 + `ApiResponse<ProductSummary>` (status = "HIDDEN")
  - [ ] 상품을 등록한 판매자가 아니면 403
  - [ ] 입력 검증: `productId` `@Positive` → 0, -1은 400 / 타입 불일치는 400
  - [ ] 상품 없음 404
  - [ ] 이미 HIDDEN인 상품 → 409 (Service mock이 던지는 예외로 검증)
  - [x] 단위 테스트 통과 (`SellerProductControllerTest` 9/9)
  - [ ] Critic APPROVE
- 메모:
  - 응답 DTO는 기존 판매자 상품 API와 같은 `ProductSummary`를 쓴다 (새 DTO를 만들지 않음)
  - 기존 `PATCH /{productId}/stock`와 경로가 겹치지 않음
  - `@ControllerSliceTest`(`addFilters = false`)에서 `@PreAuthorize`가 동작하는지는 아직 확인 안 됨 (`SellerProductController` 테스트가 하나도 없음). 동작하지 않으면 403 케이스를 TASK-006 통합 테스트로 옮기고 보고
  - 요청 바디를 받지 않기로 해서 "요청 DTO 검증"은 path variable 검증으로 대신한다 → 블로킹 이슈 #1 참고
  - 상품 이미지 삭제나 장바구니·추천 노출 제외 같은 후속 처리는 요구사항에 없어서 범위에서 뺐다
  - [Coder 기록 2026-11-29] red 확인: (1) `hide` 시그니처 추가 전 컴파일 실패 (2) 시그니처 추가 후 `@PatchMapping` 추가 전 8/8 FAILED (405 — 같은 경로에 GET만 있음). 매핑 추가 후 통과
  - [Coder 기록] `@ControllerSliceTest`에는 `SecurityConfig`(`@EnableMethodSecurity`)가 로드되지 않아 `@PreAuthorize`가 동작하지 않음(403/404 케이스가 200) → 테스트 클래스 안에 `@TestConfiguration @EnableMethodSecurity`를 `@Import`해서 켬. `sellerProductOwnerValidator`는 `@MockitoBean(name=...)`으로 등록. 403 케이스는 TASK-006으로 옮기지 않음
  - [Coder 기록] 메서드 보안을 켜니 판매자 아님이 **401**(`ACCESS_DENIED`가 `HttpStatus.UNAUTHORIZED`)로 나옴 → 사용자 승인(2026-11-29)을 받아 `ErrorCode.ACCESS_DENIED`를 **403**으로 변경. 코드(`COMMON_005`)·메시지는 그대로. 영향: `@PreAuthorize`를 쓰는 기존 엔드포인트 전체의 거부 응답이 401 → 403 (pagopa-web은 401에서만 토큰 refresh를 시도하므로 거부 시 불필요한 refresh가 사라짐)
  - [Coder 기록] 사용자 승인(2026-11-29)으로 TASK-005 범위였던 `PRODUCT_ALREADY_HIDDEN`(409, `PRODUCT_007`)을 이 태스크에서 `ErrorCode`에 추가함 (TASK-002 전례). TASK-005에서는 추가할 필요 없음
  - [Coder 기록] 404는 실제 흐름상 `@PreAuthorize`의 `isOwner` → `productApi.get()`에서 `PRODUCT_NOT_FOUND`가 나므로, validator mock이 예외를 던지게 해서 검증함 (`BusinessException`이 그대로 전파되어 404 확인)

### TASK-005: 상품 숨김 — catalog ProductApi.hide 구현 + SellerProductService 연결 (테스트 먼저)
- 상태: 🔲
- 복잡도: MEDIUM
- 의존: TASK-004
- verify: `./gradlew test --tests "com.commerce.pagopa.catalog.application.ProductApiServiceTest"`
- 구현 범위:
  - src/test/java/com/commerce/pagopa/catalog/application/ProductApiServiceTest.java (신규, **먼저** 작성 → red 확인)
  - src/main/java/com/commerce/pagopa/catalog/domain/Product.java (`hide()` 같은 상태 변경 메서드 → `ProductStatus.HIDDEN`)
  - src/main/java/com/commerce/pagopa/catalog/api/ProductApi.java (예: `ProductSummary hide(Long productId)` 메서드 추가)
  - src/main/java/com/commerce/pagopa/catalog/application/ProductApiService.java (구현, `@Transactional`)
  - src/main/java/com/commerce/pagopa/merchant/application/SellerProductService.java (`hide` 본문: `productApi.hide(productId)` 위임)
  - src/main/java/com/commerce/pagopa/global/response/ErrorCode.java (이미 HIDDEN일 때 쓸 409 에러코드 추가, 예: `PRODUCT_ALREADY_HIDDEN` — 기존 PRODUCT_00x 번호 체계를 따름)
- 완료 기준:
  - [ ] 호출 후 상품 status가 HIDDEN이고, 반환된 `ProductSummary.status`에 반영됨
  - [ ] 상품이 없으면 `PRODUCT_NOT_FOUND`(404)
  - [ ] 이미 HIDDEN인 상품이면 409 (`Product.hide()`에서 판정 — 기존 `AssistantMessage.fail()`처럼 도메인 메서드가 예외를 던지는 패턴)
  - [ ] ApplicationModulithTest 통과 (`catalog :: api` 경계 유지)
  - [ ] Critic APPROVE
- 메모:
  - merchant가 catalog 내부를 직접 참조하면 STOP 트리거라서, 상태 변경은 반드시 catalog의 공개 API를 거친다
  - `ProductApi` 인터페이스에 메서드를 추가하는 것은 HTTP 응답 시그니처 변경이 아니지만, `ProductApi`를 구현하거나 mock하는 다른 테스트가 컴파일되는지 3단계에서 확인

### TASK-006: 두 엔드포인트 401 통합 테스트 (토큰 없음 / 토큰 무효)
- 상태: 🔲
- 복잡도: MEDIUM
- 의존: TASK-003, TASK-005
- verify: `./gradlew test --tests "com.commerce.pagopa.assistant.chat.presentation.AssistantMessageDeleteAuthIntegrationTest"` + `./gradlew test --tests "com.commerce.pagopa.merchant.presentation.SellerProductHideAuthIntegrationTest"`
- 구현 범위:
  - src/test/java/com/commerce/pagopa/assistant/chat/presentation/AssistantMessageDeleteAuthIntegrationTest.java (신규, 보안 필터 활성화)
  - src/test/java/com/commerce/pagopa/merchant/presentation/SellerProductHideAuthIntegrationTest.java (신규, 보안 필터 활성화)
  - 기반: src/test/java/com/commerce/pagopa/support/testcontainers/MySqlIntegrationTest.java 재사용 여부는 Coder가 확인
- 완료 기준:
  - [ ] Authorization 헤더 없음 → 401 + `success:false`, `AUTH_001`
  - [ ] 잘못된 토큰 → 401 + `AUTH_003` (만료 토큰 → `AUTH_004`는 만들 수 있으면 추가)
  - [ ] (TASK-004에서 옮겨왔다면) 판매자가 아닌 사용자 → 403
  - [ ] Critic APPROVE
- 메모:
  - 요구사항의 "로그인 실패"는 이 엔드포인트들 기준으로 인증 실패(미인증)로 해석했다. OAuth2 로그인 플로우 자체의 실패를 뜻한다면 범위를 다시 정해야 함
  - 401 처리는 기존 보안 설정이 이미 하고 있으므로 프로덕션 코드 변경은 예상하지 않는다. 테스트가 실패하면 원인을 보고만 한다
  - Testcontainers 기반이면 Docker가 필요함
  - 테스트 클래스 이름은 가안. 기존 통합 테스트 명명 패턴이 있으면 Coder가 거기에 맞춘다

## 완료된 태스크

| ID | 태스크명 | 완료일 | Critic 판정 |
|----|---------|--------|------------|
| TASK-001 | ImageCategory 패키지를 media.domain → media.api로 이동 | 2026-11-28 | APPROVE |
| TASK-002 | 메시지 삭제 — Controller DELETE 엔드포인트 + ControllerTest | 2026-11-28 | APPROVE |
| TASK-003 | 메시지 삭제 — Service·Repository 구현 + ServiceTest | 2026-11-28 | APPROVE |

## 블로킹 이슈

| 이슈 | 태스크 | 원인 | 해결 |
|------|--------|------|------|
| #1 "요청 DTO에 검증 추가" 대상이 되는 요청 DTO가 없음 | TASK-002, TASK-004 | DELETE는 바디가 없고, 상품 PATCH는 바디 없음으로 결정됨 | 현재 계획은 path variable `@Positive` 검증으로 대신함. 요청 DTO가 꼭 필요하면 상품 PATCH에 `{status}` 바디 DTO(`@NotNull`, HIDDEN만 허용)를 다시 검토 |
