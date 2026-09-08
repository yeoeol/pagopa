# SearchHistoryService 동시성 테스트 설계

- 작성 역할: `spring-test-case-designer`
- canonical task name: `/root/spring_test_case_designer`
- runtime agent ID: 플랫폼에서 별도 숫자 ID를 제공하지 않아 canonical task name으로 실행 주체를 식별함
- 대상: `SearchHistoryService.saveHistory(Long userId, String sessionId, String keyword)`
- 기준 브랜치/커밋: `test/search-history-concurrency` / `bef0348065ff8049e768f96d4bf983965bc1c762`
- 테스트 DB: 기존 `TestcontainersConfig`의 MySQL 8.0.36
- 승인 상태: **사용자 승인 완료**
- 승인 일시: 2026-09-03 대화에서 “응 진행해”로 명시적 승인
- 승인 범위: 아래 5개 동시성 케이스와 새 대상 테스트 파일 1개
- 테스트 컨텍스트 조정: 이후 “권장안으로 수정하고 검증 계속해” 승인에 따라 실제 MySQL
  `@DataJpaTest` slice와 대상 빈 수동 `@Import`를 사용

## 보호할 동작

동일 주체와 동일하게 정규화된 검색어의 동시 저장은 MySQL upsert 뒤 정확히 한 행만 남고, 서로 다른 검색어는 각각 남아야 한다. 이 불변식이 깨지면 검색 기록 중복 또는 유실이 사용자에게 노출된다.

## 테스트 목록

| ID | 유형 | 대상 동작 | Given | When | Then (예상 assertion) | 테스트 수준 | 준비 비용 | 격리 범위 | 회귀 위험 | 수정 파일 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| N-01 | 정상 | 동일 세션의 서로 다른 검색어는 각각 저장된다 | 고유한 `sessionId` 1개와 서로 다른 검색어 8개 | 8개 요청을 동시에 저장 | 작업 8개 모두 정상 완료, 예상/예상 밖 예외 0, 최종 행 8건, keyword 집합 일치 | 실제 MySQL 통합 | 상: 컨테이너·Spring 컨텍스트 1회 | 테스트별 고유 sessionId | 서로 다른 keyword까지 덮어써 기록을 유실하는 회귀 | `src/test/java/com/commerce/pagopa/searchhistory/application/SearchHistoryServiceConcurrencyTest.java` |
| E-01 | 예외 안전성 | 이미 존재하는 검색 기록의 동시 upsert update가 예상 밖 예외 없이 완료된다 | 고유 sessionId/keyword로 `lastSearchedAt=2000-01-01T00:00:00Z`인 행 1건을 먼저 저장하고 최초 id 보관 | 같은 sessionId/keyword로 8개 요청을 동시에 저장 | 성공 8, 승인된 예상 실패 0, 예상 밖 실패 0을 각각 검증하고 최종 1건, id 불변, `lastSearchedAt`이 최초 값보다 이후 | 실제 MySQL 통합 | 상: 기존 행 준비·컨테이너 필요 | 테스트별 고유 sessionId | `ON DUPLICATE KEY UPDATE` 경합 중 DB 예외가 발생하거나 갱신 없이 성공으로 보이는 결함 | 위와 동일 |
| B-01 | 경계값 | 앞뒤 공백 제거 후 같은 검색어인 동시 요청은 중복되지 않는다 | 같은 sessionId와 `"keyword"`, `" keyword "`, `"  keyword"`, `"keyword  "`를 8개 작업에 반복 배정 | 모든 변형을 동시에 저장 | 작업 8개 모두 정상 완료, 예상/예상 밖 예외 0, 최종 1건, keyword=`"keyword"` | 실제 MySQL 통합 | 상: 공통 컨텍스트 재사용 | 테스트별 고유 sessionId | 정규화가 upsert 전에 적용되지 않아 공백 변형이 별도 행으로 남는 회귀 | 위와 동일 |
| R-01 | 회귀 | 동일 회원/동일 검색어 동시 upsert는 한 행만 남긴다 | 고유 fixture 회원 1명과 동일 keyword | 동일 userId/keyword로 8개 동시 저장 | 작업 8개 모두 정상 완료, 예상/예상 밖 예외 0, 회원 기록 1건, keyword 일치 | 실제 MySQL 통합 | 상: 회원 fixture·컨테이너 필요 | 테스트별 고유 회원 suffix/userId | `(user_id, keyword)` 제약/upsert 변경으로 중복 또는 경합 예외 재발 | 위와 동일 |
| R-02 | 회귀 | 동일 세션/동일 검색어 동시 upsert는 한 행만 남긴다 | 고유 sessionId와 동일 keyword | 동일 sessionId/keyword로 8개 동시 저장 | 작업 8개 모두 정상 완료, 예상/예상 밖 예외 0, 세션 기록 1건, keyword 일치 | 실제 MySQL 통합 | 상: 컨테이너 필요 | 테스트별 고유 sessionId | `(session_id, keyword)` 제약/upsert 변경으로 중복 또는 경합 예외 재발 | 위와 동일 |

### 예외 안전성 관점: E-01 독립 케이스

- E-01은 도메인 예외 발생 경로가 아니라 기존 행이 반드시 MySQL `ON DUPLICATE KEY UPDATE` 경로를
  타도록 준비한 update 흐름의 예외 안전성 케이스다. 동시 update가 예상 밖 DB·동시성 예외 없이
  끝나는지와 실제 갱신 결과를 함께 검증한다.
- `USER_NOT_FOUND`는 별도의 입력 계약이며 사용자가 이번 검색 기록 동시 upsert 범위에서 제외했다.
- 모든 workload에서 `성공 수 + 승인된 예상 실패 수 = 참여자 수`를 검증하고, 예상 밖 실패 수는
  별도로 `0`인지 검증한다. 현재 승인된 예상 실패는 0건이다. 예상 밖 실패가 생기면 타입·오류
  코드·메시지를 보존해 실패시키며 barrier 실패, task 내부 예외와 DB 경합 예외를 삼키지 않는다.
- `성공 + 예상 실패 + 예상 밖 실패 = 참여자 수` 집계는 outcome 누락 진단에만 사용하며 성공
  판정식을 대신하지 않는다.

## 사용자 요구사항 → 시나리오 → 예상 assertion 추적표

| 요구사항 ID | 사용자 요구사항 | 시나리오 ID | 예상 assertion |
| --- | --- | --- | --- |
| REQ-01 | 동일 `userId`와 동일 `keyword`의 동시 요청 후 검색 기록 1건 | R-01 | 성공 8, 승인된 예상 실패 0, 예상 밖 실패 0, 완료 8, 회원 기록 1건, keyword 일치 |
| REQ-02 | 동일 `sessionId`와 동일 `keyword`의 동시 요청 후 검색 기록 1건 | R-02 | 성공 8, 승인된 예상 실패 0, 예상 밖 실패 0, 완료 8, 세션 기록 1건, keyword 일치 |
| REQ-03 | 서로 다른 `keyword`의 동시 요청은 각각 저장 | N-01 | 성공 8, 승인된 예상 실패 0, 예상 밖 실패 0, 완료 8, 최종 8건, keyword 집합 일치 |
| REQ-04 | 앞뒤 공백 제거 후 같은 `keyword`의 동시 요청은 1건 | B-01 | 성공 8, 승인된 예상 실패 0, 예상 밖 실패 0, 완료 8, 최종 1건, 저장 keyword=`"keyword"` |
| REQ-05 | 기존 동일 주체·동일 keyword 행은 교체하지 않고 `lastSearchedAt` 갱신 | E-01 | 성공 8, 승인된 예상 실패 0, 예상 밖 실패 0, 완료 8, 최종 1건, id 불변, timestamp 증가 |
| REQ-06 | 예상하지 못한 예외, 미완료 작업, timeout 없음 | N-01, E-01, B-01, R-01, R-02 | `성공 + 승인된 예상 실패 = 8`, 승인된 예상 실패 0, 예상 밖 실패 0, latch 제한 시간 내 완료, 미완료 0, executor 제한 시간 내 종료 |
| REQ-07 | 정상·예외·경계값·회귀 관점으로 분류 | N-01, E-01, B-01, R-01, R-02 | 정상 1, 예외 안전성 1, 경계값 1, 회귀 2로 분류하고 각 케이스의 독립 DB 불변식 검증 |
| REQ-08 | 테스트 메서드명을 영어 `snake_case`로 일관되게 작성 | N-01, E-01, B-01, R-01, R-02 | 아래 메서드명 표의 5개 이름이 모두 영어 `snake_case` |
| REQ-09 | 기존 Testcontainers 설정과 실제 MySQL을 사용하는 동시성 통합 테스트 | N-01, E-01, B-01, R-01, R-02 | 연결 DB product가 MySQL이고 실제 native upsert·복합 유니크 제약 경로에서 최종 상태 검증 |

## 시나리오 → 사용자 요구사항 역추적표

| 시나리오 ID | 역추적 요구사항 | 핵심 assertion 계약 |
| --- | --- | --- |
| N-01 | REQ-03, REQ-06, REQ-07, REQ-08, REQ-09 | 8개 호출 성공 및 작업 완료, 실패 0, 서로 다른 keyword 8건과 집합 일치 |
| E-01 | REQ-05, REQ-06, REQ-07, REQ-08, REQ-09 | 8개 호출 성공 및 작업 완료, 실패 0, 기존 id 유지, `lastSearchedAt` 증가, 최종 1건 |
| B-01 | REQ-04, REQ-06, REQ-07, REQ-08, REQ-09 | 8개 호출 성공 및 작업 완료, 실패 0, trim 결과 keyword로 최종 1건 |
| R-01 | REQ-01, REQ-06, REQ-07, REQ-08, REQ-09 | 8개 호출 성공 및 작업 완료, 실패 0, 동일 회원 기록 1건과 keyword 일치 |
| R-02 | REQ-02, REQ-06, REQ-07, REQ-08, REQ-09 | 8개 호출 성공 및 작업 완료, 실패 0, 동일 세션 기록 1건과 keyword 일치 |

## 2026-09-08 독립 재설계 결론

- 기존 테스트 구현과 검증 산출물을 읽지 않고 원 요청, 프로젝트 컨텍스트, 빌드, 운영
  서비스·entity·repository·스키마 단서와 가장 가까운 기존 동시성 테스트만으로 요구사항과 예상
  assertion 초안을 먼저 확정했다.
- 독립 초안은 N-01, E-01, B-01, R-01, R-02의 기존 5개 케이스, 참여자 8명, 실제 MySQL
  통합 수준, 동시성 판정 계약과 의미상 차이가 없다.
- 결론: **변경 없음**. 기존 사용자 승인 범위와 승인 상태를 그대로 유지하며 재승인은 필요하지 않다.
- 이 재설계에서는 테스트 코드와 구현·검증 산출물을 읽거나 수정하지 않았다.

## 동시성 실행 계약

| 케이스 ID | 공유 자원 | 참여자 수 | 시작 gate | 유한 완료 제한 | 성공 판정 / 예상 밖 실패 | 최종 불변식 | cleanup / executor 종료 |
| --- | --- | ---: | --- | --- | --- | --- | --- |
| N-01 | 동일 MySQL 테이블과 sessionId, 서로 다른 keyword 키 | 8 | `CyclicBarrier(8)`, barrier 대기 5초 | latch 15초 내 완료, 미완료 0, `@Timeout(30초)` | 성공 8 + 승인된 예상 실패 0 = 8 / 예상 밖 실패 0 별도 검증 | 행 8건, keyword 집합 일치 | `finally`에서 세션 기록 정리; `shutdownNow()` 후 `awaitTermination(5초)==true` |
| E-01 | 이미 존재하는 동일 `(session_id, keyword)` 행 | 8 | 위와 동일 | 위와 동일 | 성공 8 + 승인된 예상 실패 0 = 8 / 예상 밖 실패 0 별도 검증 | 행 1건, 최초 id 유지, `lastSearchedAt` 증가 | 세션 기록 정리; executor 5초 내 종료 assertion |
| B-01 | trim 후 동일한 `(session_id, keyword)` 키 | 8 | 위와 동일 | 위와 동일 | 성공 8 + 승인된 예상 실패 0 = 8 / 예상 밖 실패 0 별도 검증 | 행 1건, keyword=`"keyword"` | 세션 기록 정리; executor 5초 내 종료 assertion |
| R-01 | 동일 `(user_id, keyword)` 유니크 키 | 8 | 위와 동일 | 위와 동일 | 성공 8 + 승인된 예상 실패 0 = 8 / 예상 밖 실패 0 별도 검증 | 회원 기록 1건 | 회원 검색 기록 정리; 고유 fixture suffix 사용; executor 5초 내 종료 assertion |
| R-02 | 동일 `(session_id, keyword)` 유니크 키 | 8 | 위와 동일 | 위와 동일 | 성공 8 + 승인된 예상 실패 0 = 8 / 예상 밖 실패 0 별도 검증 | 세션 기록 1건 | 세션 기록 정리; executor 5초 내 종료 assertion |

작업 수와 같은 8-thread executor를 사용해 모든 참여자가 gate에 도달하게 하되 기본 Hikari pool 고갈은 피한다. gate 통과 후 각 작업이 별도 스레드에서 서비스의 `@Transactional` 경계를 시작하므로 실제 MySQL native upsert 경로를 검증한다. 최종 상태는 주체별 조회 결과로 관찰하고 내부 SQL 호출 횟수는 검증하지 않는다.

E-01은 정확한 최종 timestamp 값을 고정하지 않는다. 각 작업이 `Instant.now()`를 독립 생성하고 InnoDB 잠금 획득 순서가 비결정적이므로, 고정된 과거 값보다 이후인지와 기존 id가 유지되는지를 안정적인 update 성공 기준으로 삼는다.

## 테스트 메서드명

영어 `snake_case`로 통일하며 camelCase 및 한국어 이름을 혼용하지 않는다.

| ID | 테스트 메서드명 |
| --- | --- |
| N-01 | `concurrent_different_keywords_are_all_saved` |
| E-01 | `concurrent_updates_of_existing_history_complete_without_errors` |
| B-01 | `concurrent_whitespace_variants_are_deduplicated` |
| R-01 | `concurrent_same_user_keyword_results_in_one_history` |
| R-02 | `concurrent_same_session_keyword_results_in_one_history` |

## 테스트 컨텍스트 선택과 트레이드오프

대상 파일에 다음 실제 MySQL JPA slice 구성을 국소 적용한다.

- `@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")`
- `@AutoConfigureTestDatabase(replace = NONE)`
- `@Import({SearchHistoryService.class, TestcontainersConfig.class, QueryDSLConfig.class})`
- `@Transactional(propagation = NOT_SUPPORTED)`

연결된 `DataSource` metadata의 DB product가 MySQL인지 공통 사전 assertion으로 확인해 H2로 잘못 실행된 테스트가 통과하지 않게 한다.

- 장점: full `@SpringBootTest`의 보안·외부 애플리케이션 설정과 분리하면서 실제
  `SearchHistoryService` transaction, repository, native upsert와 MySQL 복합 유니크 제약을
  함께 검증한다. 설정 변경은 대상 테스트 파일에만 머문다.
- 수동 import 이유: `SearchHistoryService`와 기존 `QueryDSLConfig`는 JPA slice 기본 스캔 대상이
  아니지만 서비스와 repository 구현을 실제로 연결하는 데 필요하다. `TestcontainersConfig`는 기존
  MySQL 8.0.36 연결을 재사용한다.
- transaction 선택 이유: slice의 기본 테스트 transaction을 비활성화해야 사전 데이터가 worker에게
  보이고 각 worker의 서비스 `@Transactional` 경계가 실제 commit된다.
- 단점: 컨테이너와 Spring slice 때문에 느리며 Hibernate DDL만 검증하고 운영 migration, 웹·보안
  계층 또는 full application context는 검증하지 않는다. `create-drop`은 공유·운영 DB에 사용할
  설정이 아니다.
- 선택 이유: full context는 대상과 무관한 외부 환경 설정에 막혔다. 이 slice가 A 방식 파일 경계
  안에서 실제 MySQL upsert 동작을 검증하는 가장 작은 통합 범위다.

## keyword 공백 해석

- 운영 코드는 `String.trim()`을 사용하므로 “공백을 제거하면 같은 keyword”는 **앞뒤 공백 제거 후 같은 값**으로 해석한다.
- `"key word"`와 `"keyword"`처럼 내부 공백이 다른 값은 같게 만들지 않는다. 내부 공백까지 제거하려면 운영 코드 변경이 필요하므로 이번 A 방식 범위 밖이다.

## A 방식 변경 경계

- 승인 후 새로 만들 파일: `src/test/java/com/commerce/pagopa/searchhistory/application/SearchHistoryServiceConcurrencyTest.java` 하나
- 허용: 위 5개 테스트와 그 파일 내부의 작은 실행 결과/helper, fixture 정리 코드
- 기존 테스트 setup·assertion 변경: 없음
- 금지: 운영 코드, `build.gradle`, `build.gradle.kts`, `pom.xml`, 공용 Fixture, 공용 테스트
  유틸리티, 다른 테스트 파일 수정
- 승인 전에는 테스트 코드 생성·수정·실행을 하지 않는다.

## 제외한 대안

- H2: MySQL `ON DUPLICATE KEY UPDATE`와 InnoDB 유니크 키 경합을 검증하지 못해 제외한다.
- 참여자 50/200/1000: 검색 기록 불변식보다 풀 고갈과 하네스 timeout을 측정할 위험이 있어 제외한다.
- `assertTimeoutPreemptively`: 별도 스레드와 Spring ThreadLocal 트랜잭션 부작용을 피하고 task/barrier/latch/executor 각각에 유한 제한을 둔다.
- 클래스 수준 `@Transactional`: 작업 스레드 커밋은 테스트 스레드 롤백으로 정리되지 않으므로 고유 식별자와 명시적 정리를 사용한다.
- 공용 concurrency helper 추출과 기존 테스트 리팩터링: A 방식 경계를 벗어나므로 제외한다.

## 승인 전 확인

- [x] 각 케이스가 서로 다른 결함을 잡는다.
- [x] 예외 안전성 관점은 기존 행의 update 경로와 예상 밖 예외 부재를 검증하는 독립 케이스이며,
  도메인 예외 흐름으로 보고하지 않는다.
- [x] 경계값은 실제 `trim()` 계약에서 도출했다.
- [x] 테스트 메서드명은 영어 `snake_case`로 통일했다.
- [x] 모든 workload에 참여자 수, 공유 자원, gate, 유한 제한, 성공 판정식, 예상 밖 실패의 별도
  검증, 불변식, cleanup과 executor 종료를 명시했다.
- [x] 실제 MySQL 의존성과 수정 경계를 명시했다.
- [x] 사용자에게 목록과 트레이드오프를 보여주고 명시적 승인을 받았다.

문서 라벨 변경만으로 승인하지 않으며, 대화에서 같은 범위의 명시적 승인이 필요하다.
