# SearchHistoryService 동시성 테스트 최종 검증

- 판정: **LIMITED PASS**
- 검증 대상: `SearchHistoryServiceConcurrencyTest`
- 승인 케이스: N-01, E-01, B-01, R-01, R-02
- 검증 일시: 2026-09-04 (Asia/Seoul)

## 판정 근거

대상 테스트는 실제 MySQL 8.0.36에서 최초 1회와 `--rerun-tasks` 강제 재실행 2회, 총 3회 연속 5/5 통과했다. 총 15개 케이스 실행에서 timeout, 미완료 작업, 예상·예상 밖 예외, outcome 누락, executor 미종료 또는 최종 DB 불변식 실패가 없었다.

다만 가장 가까운 기존 MySQL repository 테스트와 전체 테스트는 대상 변경과 무관한 기존 `${jwt.secret}` 미설정 때문에 full `@SpringBootTest` 컨텍스트 생성 전에 실패했다. 대상 구현과 반복 실행은 통과했지만 관련·전체 회귀 suite의 녹색 상태를 확인하지 못했으므로 PASS가 아닌 LIMITED PASS로 판정한다.

## 범위 확인

- `git diff --name-only`와 `git ls-files --others --exclude-standard`로 실제 변경 경로를 확인했다.
- 운영 코드, `build.gradle`, 공용 Fixture, 공용 테스트 유틸리티 변경은 없다.
- 승인된 코드 변경은 신규 `src/test/java/com/commerce/pagopa/searchhistory/application/SearchHistoryServiceConcurrencyTest.java` 하나에 한정된다.
- 기존 운영 `QueryDSLConfig`는 수정·복제하지 않고 대상 테스트의 `@Import`에서 재사용했다.
- suffix 축약과 concurrency helper를 포함한 모든 코드 변경은 대상 파일 내부에 있어 A 방식 파일 경계를 지켰다.
- `artifacts/00-request.md`부터 `artifacts/04-verification.md`까지는 하네스 산출물이다.
- 검증자는 테스트·운영 코드와 다른 문서를 수정하지 않고 이 검증 문서만 갱신했다.
- 최종 `git diff --check`: exit code 0. 공백 오류 없음. 문서의 LF→CRLF 경고만 출력됐다.

## 승인 목록과 실행 대조

| ID | 메서드 / 분류 | 핵심 assertion | 3회 반복 결과 |
| --- | --- | --- | --- |
| N-01 | `concurrent_different_keywords_are_all_saved` / `normal` | 8개 작업 성공, outcome·예외·완료·종료, 행 8건, keyword 집합 일치 | 3/3 PASS |
| E-01 | `concurrent_updates_of_existing_history_complete_without_errors` / `exception` | 기존 행 1건, 동시 update 8건 성공, ID 유지, `lastSearchedAt` 증가 | 3/3 PASS |
| B-01 | `concurrent_whitespace_variants_are_deduplicated` / `boundary` | 공백 변형 8개 성공, 단일 행, 정규화 keyword 일치 | 3/3 PASS |
| R-01 | `concurrent_same_user_keyword_results_in_one_history` / `regression` | 동일 userId/keyword 8개 성공, 단일 행, keyword 일치 | 3/3 PASS |
| R-02 | `concurrent_same_session_keyword_results_in_one_history` / `regression` | 동일 sessionId/keyword 8개 성공, 단일 행, keyword 일치 | 3/3 PASS |

추가 대조:

- 테스트와 lifecycle 메서드명은 영어 `snake_case`로 일관된다.
- `@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")`, `@AutoConfigureTestDatabase(replace = NONE)`, 기존 `TestcontainersConfig`가 실제 MySQL 8.0.36 연결을 구성한다.
- 매 테스트의 `@BeforeEach`에서 JDBC metadata product name이 `MySQL`인지 직접 검증한다. 3회 15개 테스트가 모두 통과했으므로 metadata assertion도 15회 통과했다.
- `@Transactional(propagation = NOT_SUPPORTED)`로 JPA slice의 기본 테스트 트랜잭션을 비활성화해 fixture commit과 worker별 `SearchHistoryService` 트랜잭션을 실제로 분리한다.
- 클래스 `@Timeout(30초)`, barrier `5초`, completion latch `15초`, executor termination `5초` 제한이 있다.
- 각 task의 `Throwable`을 수집하고 `success + expected failure + unexpected failure = 8`, expected/unexpected failure 0, success 8, 미완료 0, executor 종료를 공통 assertion으로 검증한다.
- E-01은 기존 ID 불변과 `2000-01-01T00:00:00Z`보다 `lastSearchedAt`이 증가했음을 실제 MySQL 결과로 3회 확인했다.
- R-01 suffix는 `same-user-keyword` 17자, 구분자 1자, UUID hex 12자로 총 30자다. `UserFixture`의 `nick-` 접두사까지 35자로 `User.name` 50자 제한을 만족한다.
- mock은 사용하지 않는다.

## 실행 결과

### 1. 대상 테스트 최초 실행

- 명령: `.\\gradlew.bat test --tests com.commerce.pagopa.searchhistory.application.SearchHistoryServiceConcurrencyTest`
- exit code: `0`
- 소요시간: 실행 도구 wall time 약 `25.08초`; Gradle 자체 보고 `BUILD SUCCESSFUL in 29s`
- 결과: 5/5 PASS

### 2. 강제 반복 실행 1

- 명령: `.\\gradlew.bat test --tests com.commerce.pagopa.searchhistory.application.SearchHistoryServiceConcurrencyTest --rerun-tasks`
- exit code: `0`
- 소요시간: 실행 도구 wall time 약 `27.21초`; Gradle 자체 보고 `BUILD SUCCESSFUL in 31s`
- 결과: 5/5 PASS

### 3. 강제 반복 실행 2

- 명령: `.\\gradlew.bat test --tests com.commerce.pagopa.searchhistory.application.SearchHistoryServiceConcurrencyTest --rerun-tasks`
- exit code: `0`
- 소요시간: 실행 도구 wall time 약 `28.21초`; Gradle 자체 보고 `BUILD SUCCESSFUL in 31s`
- 결과: 5/5 PASS
- 최종 반복 XML suite 시간: `23.219초`
- flaky 신호: 없음

### 4. 관련 MySQL repository 테스트

- 선택 근거: 검색 기록 관련 기존 테스트가 없어 동일 `TestcontainersConfig`와 실제 MySQL을 사용하는 `OrderRepositoryTest`, `ProductRepositoryTest`를 가장 가까운 repository 회귀 범위로 선택했다. 참여자 50/200/1000의 `StockConcurrencyTest`는 별도 대상 실행에서는 비용이 커 전체 테스트 범위에서 상태를 확인했다.
- 명령: `.\\gradlew.bat test --tests com.commerce.pagopa.order.infrastructure.persistence.OrderRepositoryTest --tests com.commerce.pagopa.product.infrastructure.persistence.ProductRepositoryTest`
- exit code: `1`
- 소요시간: 실행 도구 wall time 약 `18.63초`; Gradle 자체 보고 `BUILD FAILED in 21s`
- 결과: 10 tests completed, 10 failed
- 최초 원인: `PlaceholderResolutionException: Could not resolve placeholder 'jwt.secret' in value "${jwt.secret}"`
- 해석: 기존 두 테스트의 full `@SpringBootTest`가 컨텍스트 생성 전에 실패해 repository assertion은 실행되지 않았다. 대상 테스트 변경에 의한 assertion 실패 근거는 없다.

### 5. 전체 테스트

- 명령: `.\\gradlew.bat test`
- exit code: `1`
- 소요시간: 실행 도구 wall time 약 `49.18초`; Gradle 자체 보고 `BUILD FAILED in 53s`
- 결과: 36 tests completed, 23 failed, 13 passed
- PASS: `SearchHistoryServiceConcurrencyTest` 5건, auth 단위 테스트 8건
- FAIL: `OrderRepositoryTest` 4건, `ProductRepositoryTest` 6건, `PagopaApplicationTests` 1건, `StockConcurrencyTest` 12건
- 실패 원인: full `@SpringBootTest` 컨텍스트의 `${jwt.secret}` 미설정. 최초 full-context 실패와 별도 dynamic-property context인 `StockConcurrencyTest`에서 같은 placeholder 오류를 확인했고, 공유 context의 나머지는 Spring failure threshold로 중단됐다.
- 대상 테스트의 전체 suite 결과: 5 tests, failures 0, errors 0, skipped 0, XML suite time `17.241초`

## 가독성·안정성 및 회귀 위험

| 심각도 | 위치 | 근거 | 판단 / 남은 위험 |
| --- | --- | --- | --- |
| 없음 | 대상 테스트 전체 | 3회 연속 15/15 통과, 모든 task outcome과 자원 종료 assertion 포함 | 관찰된 flaky 신호 없음 |
| 낮음 | R-01 fixture lifecycle | 검색 기록은 정리하지만 UUID 회원 fixture는 컨텍스트 DB에 남는다 | 축약 UUID로 케이스 충돌 가능성은 낮고 컨테이너 종료 시 제거됨 |
| 중간 | `artifacts/02-test-cases.md:58` / 구현 파일 | 승인 설계 문서는 `@SpringBootTest`를 명시하지만 최종 구현은 실제 MySQL `@DataJpaTest` slice다 | 승인된 동작과 A 방식 경계는 유지하지만 설계 문서와 최종 컨텍스트 선택이 불일치 |
| 기존 저장소 한계 | full `@SpringBootTest` 테스트 | `${jwt.secret}` 미설정으로 관련 10건과 전체 23건이 본문 전에 실패 | 대상 변경의 회귀 여부가 아니라 기존 full-context 테스트 환경의 녹색 상태를 확인할 수 없음 |

## 미검증 범위

- 관련 repository 및 기존 `StockConcurrencyTest`의 assertion 본문은 `${jwt.secret}` 미설정 때문에 실행되지 않았다.
- 운영 migration과의 일치는 검증하지 않는다. 대상 테스트는 승인된 대로 Hibernate `create-drop` 스키마에서 MySQL native upsert와 유니크 제약을 검증한다.
- 전체 suite가 녹색이 아니므로 최종 PASS 조건은 충족하지 않는다.
